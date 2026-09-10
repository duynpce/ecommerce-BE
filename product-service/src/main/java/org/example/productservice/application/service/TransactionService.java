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
import org.example.productservice.application.repository.ShopRepository;
import org.example.productservice.application.repository.TransactionRepository;
import org.example.productservice.application.usecase.SubOrderUseCase;
import org.example.productservice.application.usecase.TransactionUseCase;
import org.example.productservice.application.usecase.VoucherUseCase;
import org.example.productservice.application.command.VoucherLine;
import org.example.productservice.domain.constant.ProductStatus;
import org.example.productservice.domain.constant.TransactionStatus;
import org.example.productservice.domain.exception.InvalidStateException;
import org.example.productservice.domain.exception.NotFoundException;
import org.example.productservice.domain.model.Product;
import org.example.productservice.domain.model.ProductSnapshot;
import org.example.productservice.domain.model.Shop;
import org.example.productservice.domain.model.SubOrder;
import org.example.productservice.domain.model.Transaction;
import org.example.productservice.domain.model.VoucherApplication;
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
    private final ShopRepository shopRepository;
    private final SubOrderUseCase subOrderUseCase;
    private final TransactionMapper transactionMapper;
    private final SubOrderMapper subOrderMapper;
    private final TicketClient ticketClient;
    private final VoucherUseCase voucherUseCase;

    @Override
    @Transactional
    public Transaction create(CreateTransactionCommand command) {
        if (command.items() == null || command.items().isEmpty()) {
            throw new IllegalArgumentException("Transaction must contain at least one item");
        }
        if (command.phoneNumber() == null || command.phoneNumber().isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }
        if (command.address() == null || command.address().isBlank()) {
            throw new IllegalArgumentException("Delivery address is required");
        }

        // 1. Group requested items by shop and remember the contributor for each shop.
        // SubOrderService.create owns snapshot creation, stock deduction, totals, and persistence.
        Map<UUID, List<CreateSubOrderCommand.Item>> shopItemsMap = new LinkedHashMap<>();
        Map<UUID, UUID> shopContributorMap = new HashMap<>();
        Map<UUID, Integer> requestedQuantityByProduct = new HashMap<>();
        Map<UUID, Product> productsById = new LinkedHashMap<>();

        for (var itemReq : command.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + itemReq.productId()));
            productsById.put(product.getId(), product);

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

        List<VoucherApplication> voucherApplications = new ArrayList<>();
        if (command.voucherCodes() != null && !command.voucherCodes().isEmpty()) {
            List<VoucherLine> voucherLines = command.items().stream()
                    .map(item -> new VoucherLine(productsById.get(item.productId()), item.quantity()))
                    .toList();
            Set<org.example.productservice.domain.constant.VoucherType> selectedTypes =
                    EnumSet.noneOf(org.example.productservice.domain.constant.VoucherType.class);
            Set<String> selectedCodes = new HashSet<>();
            for (String code : command.voucherCodes()) {
                String normalizedCode = code.trim().toUpperCase(Locale.ROOT);
                if (!selectedCodes.add(normalizedCode)) {
                    throw new InvalidStateException("The same voucher cannot be applied more than once");
                }
                VoucherApplication application = voucherUseCase.preview(normalizedCode, voucherLines);
                if (!selectedTypes.add(application.voucher().getType())) {
                    throw new InvalidStateException("Only one " + application.voucher().getType()
                            + " voucher can be applied per transaction");
                }
                voucherApplications.add(application);
            }
        }

        // 2. Create parent transaction
        UUID transactionId = UUID.randomUUID();
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setCustomerId(command.customerId());
        transaction.setPhoneNumber(command.phoneNumber().trim());
        transaction.setAddress(command.address().trim());
        transaction.setCreatedAt(Instant.now());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.applyVouchers(voucherApplications.stream().map(VoucherApplication::snapshot).toList());

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
        voucherApplications.forEach(voucherUseCase::consume);

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
    public Transaction complete(
            UUID id,
            TransactionStatus status,
            String reason,
            UUID triggerSubOrderId) {
        if (status == null) {
            throw new IllegalArgumentException("Transaction status cannot be null");
        }

        Transaction transaction = requireTransaction(id);

        requireStatus(transaction, TransactionStatus.PENDING, "complete");
        String sourceReason = normalizeReason(reason, status);
        String triggerDescription = describeSubOrder(triggerSubOrderId);

        transaction.setStatus(status);
        transaction.setStatusReason(transactionReason(status, sourceReason, triggerDescription));
        transaction.setTriggerSubOrderId(triggerSubOrderId);

        if (status == TransactionStatus.CANCELLED || status == TransactionStatus.REJECTED) {
            cascadeCancellation(
                    transaction, status, triggerSubOrderId, sourceReason, triggerDescription);
        }

        return transactionRepository.save(transaction);
    }

    private void cascadeCancellation(
            Transaction transaction,
            TransactionStatus terminalStatus,
            UUID triggerSubOrderId,
            String sourceReason,
            String triggerDescription) {
        for (UUID subOrderId : transaction.getSubOrderIds()) {
            boolean isTrigger = subOrderId.equals(triggerSubOrderId);
            String subOrderReason;
            if (terminalStatus == TransactionStatus.REJECTED && triggerSubOrderId != null) {
                subOrderReason = isTrigger
                        ? "This shop order caused the transaction rejection. " + sourceReason
                        : "Cancelled because " + triggerDescription
                                + " was rejected. " + sourceReason;
            } else if (triggerSubOrderId != null) {
                subOrderReason = isTrigger
                        ? "This shop order caused the transaction cancellation. " + sourceReason
                        : "Cancelled because " + triggerDescription
                                + " was cancelled. " + sourceReason;
            } else {
                subOrderReason = "Cancelled because the parent transaction ended. " + sourceReason;
            }
            subOrderUseCase.cancelForTransactionTermination(subOrderId, subOrderReason);
        }
    }

    private String transactionReason(
            TransactionStatus status,
            String sourceReason,
            String triggerDescription) {
        if (triggerDescription == null) {
            return sourceReason;
        }
        if (status == TransactionStatus.REJECTED) {
            return "Transaction rejected because " + triggerDescription
                    + " was rejected. " + sourceReason;
        }
        if (status == TransactionStatus.CANCELLED) {
            return "Transaction cancelled because " + triggerDescription
                    + " was cancelled. " + sourceReason;
        }
        return sourceReason;
    }

    /** Builds customer-facing context from names while keeping UUIDs as metadata only. */
    private String describeSubOrder(UUID subOrderId) {
        if (subOrderId == null) {
            return null;
        }

        try {
            SubOrder subOrder = subOrderUseCase.findById(subOrderId);
            if (subOrder == null) {
                return "one shop order";
            }

            String shopName = shopRepository.findById(subOrder.getShopId())
                    .map(Shop::getName)
                    .filter(name -> !name.isBlank())
                    .orElse(null);
            String productNames = subOrder.getItems().stream()
                    .map(ProductSnapshot::getName)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(name -> !name.isBlank())
                    .distinct()
                    .collect(java.util.stream.Collectors.joining(", "));

            if (shopName != null && !productNames.isBlank()) {
                return "shop \"" + shopName + "\" for " + productNames;
            }
            if (shopName != null) {
                return "shop \"" + shopName + "\"";
            }
            if (!productNames.isBlank()) {
                return "the shop order for " + productNames;
            }
        } catch (RuntimeException exception) {
            log.warn("Could not resolve display names for terminal sub-order {}", subOrderId, exception);
        }
        return "one shop order";
    }

    private String normalizeReason(String reason, TransactionStatus status) {
        if (reason != null && !reason.isBlank()) {
            return reason.trim();
        }
        if (status == TransactionStatus.REJECTED) {
            return "The transaction was rejected by the workflow.";
        }
        if (status == TransactionStatus.CANCELLED) {
            return "The transaction was cancelled by the workflow.";
        }
        return null;
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
