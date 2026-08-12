package org.example.productservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.productservice.application.client.TicketClient;
import org.example.productservice.application.command.CreateTransactionCommand;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateTransactionCommand;
import org.example.productservice.application.criteria.TransactionSearchCriteria;
import org.example.productservice.application.mapper.TransactionMapper;
import org.example.productservice.application.repository.ProductRepository;
import org.example.productservice.application.repository.ShopRepository;
import org.example.productservice.application.repository.SubOrderRepository;
import org.example.productservice.application.repository.TransactionRepository;
import org.example.productservice.application.usecase.TransactionUseCase;
import org.example.productservice.domain.constant.ProductSnapshotStatus;
import org.example.productservice.domain.constant.ProductStatus;
import org.example.productservice.domain.constant.SubOrderStatus;
import org.example.productservice.domain.constant.TransactionStatus;
import org.example.productservice.domain.exception.InvalidStateException;
import org.example.productservice.domain.exception.NotFoundException;
import org.example.productservice.domain.model.Product;
import org.example.productservice.domain.model.ProductSnapshot;
import org.example.productservice.domain.model.Shop;
import org.example.productservice.domain.model.SubOrder;
import org.example.productservice.domain.model.Transaction;
import org.example.productservice.infrastructure.ticket.dto.StartBuyingProcedureRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService implements TransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final SubOrderRepository subOrderRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final TransactionMapper transactionMapper;
    private final TicketClient ticketClient;

    @Override
    @Transactional
    public Transaction create(CreateTransactionCommand command) {
        if (command.items() == null || command.items().isEmpty()) {
            throw new IllegalArgumentException("Transaction must contain at least one item");
        }

        // 1. Group snapshots by shopId and validate stock
        Map<UUID, List<ProductSnapshot>> shopSnapshotsMap = new HashMap<>();

        for (var itemReq : command.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + itemReq.productId()));

            if (product.getQuantity() < itemReq.quantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + itemReq.productId());
            }

            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new IllegalArgumentException("Cannot create transaction for inactive product: " + itemReq.productId());
            }

            UUID shopId = product.getShopId();
            if (shopId == null) {
                throw new NotFoundException("Shop not found for product: " + product.getId());
            }

            ProductSnapshot snapshot = ProductSnapshot.of(product, itemReq.quantity());
            snapshot.setStatus(ProductSnapshotStatus.PENDING);

            shopSnapshotsMap.computeIfAbsent(shopId, k -> new ArrayList<>()).add(snapshot);

            // Deduct stock
            product.setQuantity(product.getQuantity() - itemReq.quantity());
            productRepository.save(product);
        }

        // 2. Create parent transaction
        UUID transactionId = UUID.randomUUID();
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setCustomerId(command.customerId());
        transaction.setCreatedAt(Instant.now());
        transaction.setStatus(TransactionStatus.PENDING);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // 3. Create sub-orders per shop
        List<SubOrder> createdSubOrders = new ArrayList<>();
        for (Map.Entry<UUID, List<ProductSnapshot>> entry : shopSnapshotsMap.entrySet()) {
            UUID shopId = entry.getKey();
            List<ProductSnapshot> snapshots = entry.getValue();

            SubOrder subOrder = new SubOrder(
                    UUID.randomUUID(),
                    savedTransaction.getId(),
                    shopId,
                    command.customerId(),
                    snapshots,
                    BigDecimal.ZERO,
                    null
            );
            subOrder.setCreatedAt(Instant.now());
            subOrder.setStatus(SubOrderStatus.PENDING);

            SubOrder savedSubOrder = subOrderRepository.save(subOrder);
            createdSubOrders.add(savedSubOrder);
            savedTransaction.addSubOrderId(savedSubOrder.getId());
        }

        // 4. Recalculate transaction aggregate total
        savedTransaction.recalculateTotal(createdSubOrders);
        Transaction finalTransaction = transactionRepository.save(savedTransaction);

        // 5. Trigger ticket process
        ticketClient.startBuyingProcedure(new StartBuyingProcedureRequest(
                finalTransaction.getId(),
                null,
                finalTransaction.getCustomerId()
        ));

        return finalTransaction;
    }

    @Override
    @Transactional(readOnly = true)
    public Transaction findById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found: " + id));
    }

    @Override
    @Transactional
    public Transaction update(UpdateTransactionCommand command) {
        Transaction transaction = transactionRepository.findById(command.id())
                .orElseThrow(() -> new NotFoundException("Transaction not found: " + command.id()));

        transactionMapper.updateFromCommand(command, transaction);
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!transactionRepository.existsById(id)) {
            throw new NotFoundException("Transaction not found: " + id);
        }
        transactionRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageCommand<Transaction> search(TransactionSearchCriteria criteria) {
        return transactionRepository.search(criteria);
    }

    @Override
    @Transactional
    public Transaction approve(UUID id) {
        Transaction transaction = requireTransaction(id);
        requireStatus(transaction, TransactionStatus.PENDING, "approve");
        transaction.setStatus(TransactionStatus.PACKING);

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction reject(UUID id) {
        Transaction transaction = requireTransaction(id);
        requireStatus(transaction, TransactionStatus.PENDING, "reject");
        transaction.setStatus(TransactionStatus.REJECTED);
        transactionRepository.save(transaction);

        List<SubOrder> subOrders = subOrderRepository.findByTransactionId(transaction.getId());
        for (SubOrder subOrder : subOrders) {
            subOrder.setStatus(SubOrderStatus.CANCELLED);
            subOrderRepository.save(subOrder);
            for (ProductSnapshot item : subOrder.getItems()) {
                productRepository.findById(item.getProductId()).ifPresent(product -> {
                    product.setQuantity(product.getQuantity() + item.getQuantity());
                    productRepository.save(product);
                });
            }
        }

        return transaction;
    }

    @Override
    @Transactional
    public Transaction markShipped(UUID id) {
        Transaction transaction = requireTransaction(id);
        requireStatus(transaction, TransactionStatus.PACKING, "mark as shipped");
        transaction.setStatus(TransactionStatus.DELIVERING);

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction complete(UUID id) {
        Transaction transaction = requireTransaction(id);

        List<SubOrder> subOrders = subOrderRepository.findByTransactionId(transaction.getId());
        for (SubOrder subOrder : subOrders) {
            subOrder.setStatus(SubOrderStatus.COMPLETED);
            for (ProductSnapshot item : subOrder.getItems()) {
                item.setStatus(ProductSnapshotStatus.COMPLETED);
                Product product = productRepository.findByIdWithShop(item.getProductId())
                        .orElseThrow(() -> new NotFoundException("Product not found: " + item.getProductId()));
                Shop shop = product.getShop();
                if (shop != null) {
                    shop.incrementSoldQuantity(item.getQuantity());
                    shopRepository.save(shop);
                }
                product.incrementSoldQuantity(item.getQuantity());
                productRepository.save(product);
            }
            subOrderRepository.save(subOrder);
        }

        requireStatus(transaction, TransactionStatus.DELIVERING, "complete");
        transaction.setStatus(TransactionStatus.COMPLETED);

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction returnTransaction(UUID id) {
        Transaction transaction = requireTransaction(id);

        if (transaction.getStatus() == TransactionStatus.RETURNED) {
            return transaction;
        }

        transaction.setStatus(TransactionStatus.RETURNED);
        transactionRepository.save(transaction);

        List<SubOrder> subOrders = subOrderRepository.findByTransactionId(transaction.getId());
        for (SubOrder subOrder : subOrders) {
            subOrder.setStatus(SubOrderStatus.RETURNED);
            for (ProductSnapshot item : subOrder.getItems()) {
                item.setStatus(ProductSnapshotStatus.RETURNED);
                productRepository.findById(item.getProductId()).ifPresent(product -> {
                    product.setQuantity(product.getQuantity() + item.getQuantity());
                    productRepository.save(product);
                });
            }
            subOrderRepository.save(subOrder);
        }

        return transaction;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Transaction requireTransaction(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found: " + id));
    }

    private void requireStatus(Transaction transaction, TransactionStatus expected, String action) {
        if (transaction.getStatus() != expected) {
            throw new InvalidStateException(String.format(
                    "Cannot %s transaction %s: expected status %s but was %s",
                    action, transaction.getId(), expected, transaction.getStatus()
            ));
        }
    }
}
