package org.example.productservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.productservice.application.client.TicketClient;
import org.example.productservice.application.command.CreateSubOrderCommand;
import org.example.productservice.application.command.CreateTransactionCommand;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateTransactionCommand;
import org.example.productservice.application.criteria.TransactionSearchCriteria;
import org.example.productservice.application.mapper.SubOrderMapper;
import org.example.productservice.application.mapper.TransactionMapper;
import org.example.productservice.application.repository.ProductRepository;
import org.example.productservice.application.repository.TransactionRepository;
import org.example.productservice.application.usecase.SubOrderUseCase;
import org.example.productservice.application.usecase.TransactionUseCase;
import org.example.productservice.domain.constant.ProductStatus;
import org.example.productservice.domain.constant.TransactionStatus;
import org.example.productservice.domain.exception.InvalidStateException;
import org.example.productservice.domain.exception.NotFoundException;
import org.example.productservice.domain.model.Product;
import org.example.productservice.domain.model.SubOrder;
import org.example.productservice.domain.model.Transaction;
import org.example.productservice.infrastructure.ticket.dto.StartBuyingProcedureRequest;
import org.example.productservice.infrastructure.web.dto.suborder.SubOrderResponse;
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
    private final ProductRepository productRepository;
    private final SubOrderUseCase subOrderUseCase;
    private final TransactionMapper transactionMapper;
    private final SubOrderMapper subOrderMapper;
    private final TicketClient ticketClient;

    @Override
    @Transactional
    public Transaction create(CreateTransactionCommand command) {
        if (command.items() == null || command.items().isEmpty()) {
            throw new IllegalArgumentException("Transaction must contain at least one item");
        }

        // 1. Group requested items by shop and remember the contributor for each shop.
        // SubOrderService.create owns snapshot creation, stock deduction, totals, and persistence.
        Map<UUID, List<CreateSubOrderCommand.Item>> shopItemsMap = new LinkedHashMap<>();
        Map<UUID, UUID> shopContributorMap = new HashMap<>();
        Map<UUID, Integer> requestedQuantityByProduct = new HashMap<>();

        for (var itemReq : command.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + itemReq.productId()));

            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new InvalidStateException("Product " + product.getName() + " is not active");
            }

            int requestedQuantity = requestedQuantityByProduct.merge(
                    product.getId(),
                    itemReq.quantity(),
                    Integer::sum
            );

            if (product.getQuantity() < requestedQuantity) {
                throw new InvalidStateException("Insufficient stock for product " + product.getName()
                        + ". Requested: " + requestedQuantity + ", available: " + product.getQuantity());
            }

            UUID shopId = product.getShopId();
            if (shopId == null) {
                throw new NotFoundException("Shop not found for product: " + product.getId());
            }

            UUID contributorId = product.getContributorId();
            if (contributorId == null) {
                throw new InvalidStateException("Contributor not found for product: " + product.getId());
            }

            UUID mappedContributorId = shopContributorMap.putIfAbsent(shopId, contributorId);

            if (mappedContributorId != null && !mappedContributorId.equals(contributorId)) {
                throw new InvalidStateException("Products in shop " + shopId + " have inconsistent contributors");
            }

            shopItemsMap.computeIfAbsent(shopId, ignored -> new ArrayList<>())
                    .add(new CreateSubOrderCommand.Item(product.getId(), itemReq.quantity()));
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
        for (Map.Entry<UUID, List<CreateSubOrderCommand.Item>> entry : shopItemsMap.entrySet()) {
            UUID shopId = entry.getKey();
            SubOrder savedSubOrder = subOrderUseCase.create(new CreateSubOrderCommand(
                    savedTransaction.getId(),
                    shopId,
                    command.customerId(),
                    shopContributorMap.get(shopId),
                    BigDecimal.ZERO,
                    null,
                    entry.getValue()
            ));

            createdSubOrders.add(savedSubOrder);
            savedTransaction.addSubOrderId(savedSubOrder.getId());
        }

        // 4. Recalculate transaction aggregate total
        savedTransaction.recalculateTotal(createdSubOrders);
        Transaction finalTransaction = transactionRepository.save(savedTransaction);

        // Map sub-orders to response DTOs containing snapshots
        List<SubOrderResponse> subOrderResponses =
                createdSubOrders.stream()
                        .map(subOrderMapper::toResponse)
                        .toList();

        // 5. Trigger ticket process with transaction ID and sub-orders list
        ticketClient.startBuyingProcedure(new StartBuyingProcedureRequest(
                finalTransaction.getId(),
                finalTransaction.getCustomerId(),
                subOrderResponses
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
    public Transaction complete(UUID id, TransactionStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Transaction status cannot be null");
        }

        Transaction transaction = requireTransaction(id);

        requireStatus(transaction, TransactionStatus.PENDING, "complete");
        transaction.setStatus(status);

        return transactionRepository.save(transaction);
    }

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
