package org.example.productservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.productservice.application.command.CreateSubOrderCommand;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateSubOrderCommand;
import org.example.productservice.application.criteria.SubOrderSearchCriteria;
import org.example.productservice.application.repository.ProductRepository;
import org.example.productservice.application.repository.ShopRepository;
import org.example.productservice.application.repository.SubOrderRepository;
import org.example.productservice.application.repository.TransactionRepository;
import org.example.productservice.application.usecase.SubOrderUseCase;
import org.example.productservice.domain.constant.ProductSnapshotStatus;
import org.example.productservice.domain.constant.ProductStatus;
import org.example.productservice.domain.constant.SubOrderStatus;
import org.example.productservice.domain.constant.TransactionStatus;
import org.example.productservice.domain.exception.NotFoundException;
import org.example.productservice.domain.model.Product;
import org.example.productservice.domain.model.ProductSnapshot;
import org.example.productservice.domain.model.Shop;
import org.example.productservice.domain.model.SubOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubOrderService implements SubOrderUseCase {

    private final SubOrderRepository subOrderRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public SubOrder create(CreateSubOrderCommand command) {
        log.info("Creating sub-order for shopId: {} and customerId: {}", command.shopId(), command.customerId());

        if (!shopRepository.existsById(command.shopId())) {
            throw new NotFoundException("Shop not found: " + command.shopId());
        }

        if (command.items() == null || command.items().isEmpty()) {
            throw new IllegalArgumentException("Sub-order must contain at least one item");
        }

        List<ProductSnapshot> snapshots = new ArrayList<>();

        for (var itemReq : command.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + itemReq.productId()));

            if (!command.shopId().equals(product.getShopId())) {
                throw new IllegalArgumentException(String.format("Product %s does not belong to shop %s", itemReq.productId(), command.shopId()));
            }

            if (product.getQuantity() < itemReq.quantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + itemReq.productId());
            }

            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new IllegalArgumentException("Cannot order inactive product: " + itemReq.productId());
            }

            ProductSnapshot snapshot = ProductSnapshot.of(product, itemReq.quantity());
            snapshots.add(snapshot);

            // Deduct stock
            product.setQuantity(product.getQuantity() - itemReq.quantity());
            productRepository.save(product);
        }

        SubOrder subOrder = new SubOrder(
                UUID.randomUUID(),
                command.transactionId(),
                command.shopId(),
                command.customerId(),
                command.contributorId(),
                snapshots,
                command.shippingFee(),
                command.note()
        );
        subOrder.setCreatedAt(Instant.now());

        return subOrderRepository.save(subOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public SubOrder findById(UUID id) {
        return subOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sub-order not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubOrder> findByTransactionId(UUID transactionId) {
        return subOrderRepository.findByTransactionId(transactionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubOrder> findByShopId(UUID shopId) {
        return subOrderRepository.findByShopId(shopId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubOrder> findByCustomerId(UUID customerId) {
        return subOrderRepository.findByCustomerId(customerId);
    }

    @Override
    @Transactional
    public SubOrder update(UpdateSubOrderCommand command) {
        SubOrder subOrder = findById(command.id());

        if (command.shippingFee() != null) {
            subOrder.setShippingFee(command.shippingFee());
        }
        if (command.note() != null) {
            subOrder.setNote(command.note());
        }
        if (command.status() != null) {
            subOrder.setStatus(command.status());
        }

        return subOrderRepository.save(subOrder);
    }

    @Override
    @Transactional
    public SubOrder updateStatus(UUID id, SubOrderStatus newStatus) {
        SubOrder subOrder = findById(id);
        SubOrderStatus oldStatus = subOrder.getStatus();

        if (oldStatus == newStatus) {
            return subOrder;
        }

        // Consolidation is monotonic. A late completion from another parallel
        // Camunda branch must not move an already-ready parcel back to waiting.
        if (oldStatus == SubOrderStatus.AWAITING_PICKUP
                && newStatus == SubOrderStatus.WAITING_FOR_CONSOLIDATION) {
            return subOrder;
        }

        if ((newStatus == SubOrderStatus.WAITING_FOR_CONSOLIDATION
                || newStatus == SubOrderStatus.AWAITING_PICKUP)
                && isTerminal(oldStatus)) {
            throw new IllegalStateException(
                    "Cannot move terminal sub-order " + id
                            + " back into the delivery workflow: " + oldStatus);
        }

        subOrder.setStatus(newStatus);

        // If sub-order is cancelled or returned, restore product stock
        if (newStatus == SubOrderStatus.CANCELLED || newStatus == SubOrderStatus.RETURNED || newStatus == SubOrderStatus.REJECTED) {
            for (ProductSnapshot item : subOrder.getItems()) {
                productRepository.findById(item.getProductId()).ifPresent(product -> {
                    product.setQuantity(product.getQuantity() + item.getQuantity());
                    productRepository.save(product);
                });
            }
        }

        return subOrderRepository.save(subOrder);
    }

    private boolean isTerminal(SubOrderStatus status) {
        return status == SubOrderStatus.CANCELLED
                || status == SubOrderStatus.REJECTED
                || status == SubOrderStatus.COMPLETED
                || status == SubOrderStatus.RETURNED
                || status == SubOrderStatus.PARTIALLY_RETURNED;
    }

    @Override
    @Transactional
    public SubOrder updateSnapshotStatus(UUID subOrderId, UUID snapshotId, ProductSnapshotStatus newStatus) {
        SubOrder subOrder = findById(subOrderId);

        // Cancellation and rejection are sub-order decisions. Never allow the
        // generic snapshot endpoint to leave the rest of the sub-order active.
        if (newStatus == ProductSnapshotStatus.CANCELLED) {
            return cancel(subOrderId, null);
        }
        if (newStatus == ProductSnapshotStatus.REJECTED) {
            return reject(subOrderId, null);
        }

        ProductSnapshot targetSnapshot = subOrder.getItems().stream()
                .filter(item -> item.getId().equals(snapshotId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Product snapshot not found for ID: " + snapshotId));
        ProductSnapshotStatus oldStatus = targetSnapshot.getStatus();
        if (oldStatus == newStatus) {
            return subOrder;
        }

        subOrder.updateSnapshotStatus(snapshotId, newStatus);

        // Restore stock once when an individual snapshot is returned.
        if (newStatus == ProductSnapshotStatus.RETURNED) {
            productRepository.findById(targetSnapshot.getProductId()).ifPresent(product -> {
                product.setQuantity(product.getQuantity() + targetSnapshot.getQuantity());
                productRepository.save(product);
            });
        } else if (newStatus == ProductSnapshotStatus.COMPLETED) {
            // Increment sold count
            productRepository.findByIdWithShop(targetSnapshot.getProductId()).ifPresent(product -> {
                product.incrementSoldQuantity(targetSnapshot.getQuantity());
                if (product.getShop() != null) {
                    Shop shop = product.getShop();
                    shop.incrementSoldQuantity(targetSnapshot.getQuantity());
                    shopRepository.save(shop);
                }
                productRepository.save(product);
            });
        }


        return subOrderRepository.save(subOrder);
    }

    @Override
    @Transactional
    public SubOrder returnSnapshot(UUID id, UUID snapshotId) {
        return updateSnapshotStatus(id, snapshotId, ProductSnapshotStatus.RETURNED);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!subOrderRepository.existsById(id)) {
            throw new NotFoundException("Sub-order not found: " + id);
        }
        subOrderRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageCommand<SubOrder> search(SubOrderSearchCriteria criteria) {
        return subOrderRepository.search(criteria);
    }

    // -------------------------------------------------------------------------
    // Camunda state-transition operations
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public SubOrder approve(UUID id) {
        SubOrder subOrder = findById(id);
        if (subOrder.getStatus() != SubOrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot approve sub-order " + id + ": expected PENDING but was " + subOrder.getStatus());
        }

        subOrder.setStatus(SubOrderStatus.PENDING); // stays PENDING until delivery; snapshots go PACKING
        for (ProductSnapshot item : subOrder.getItems()) {
            item.setStatus(ProductSnapshotStatus.PACKING);
        }

        log.info("[sub-order] Approved (PACKING): subOrderId={}", id);
        return subOrderRepository.save(subOrder);
    }

    @Override
    @Transactional
    public SubOrder reject(UUID id, String reason) {
        SubOrder subOrder = findById(id);
        if (subOrder.getStatus() == SubOrderStatus.REJECTED) {
            return subOrder;
        }
        if (subOrder.getStatus() != SubOrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot reject sub-order " + id + ": expected PENDING but was " + subOrder.getStatus());
        }
        boolean allSnapshotsPending = subOrder.getItems().stream()
                .allMatch(item -> item.getStatus() == ProductSnapshotStatus.PENDING);
        if (!allSnapshotsPending) {
            throw new IllegalStateException(
                    "Cannot reject sub-order " + id + ": one or more snapshots are no longer PENDING");
        }

        subOrder.setStatus(SubOrderStatus.REJECTED);
        subOrder.setStatusReason(normalizeReason(reason, "Rejected by the contributor."));
        for (ProductSnapshot item : subOrder.getItems()) {
            item.setStatus(ProductSnapshotStatus.REJECTED);
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                product.setQuantity(product.getQuantity() + item.getQuantity());
                productRepository.save(product);
            });
        }

        log.info("[sub-order] Rejected and stock restored: subOrderId={}", id);
        return subOrderRepository.save(subOrder);
    }

    @Override
    @Transactional
    public SubOrder cancel(UUID id, String reason) {
        return cancelInternal(id, reason, false);
    }

    @Override
    @Transactional
    public SubOrder cancelForTransactionTermination(UUID id, String reason) {
        return cancelInternal(id, reason, true);
    }

    private SubOrder cancelInternal(UUID id, String reason, boolean transactionTermination) {
        SubOrder subOrder = findById(id);

        if (subOrder.getStatus() == SubOrderStatus.CANCELLED) {
            subOrder.setStatusReason(normalizeReason(reason, subOrder.getStatusReason()));
            return subOrderRepository.save(subOrder);
        }

        if (subOrder.getStatus() == SubOrderStatus.REJECTED) {
            // reject() already restored stock. A parent transaction cascade
            // only normalizes the final status and must not restore it twice.
            subOrder.setStatus(SubOrderStatus.CANCELLED);
            subOrder.setStatusReason(normalizeReason(reason, subOrder.getStatusReason()));
            for (ProductSnapshot item : subOrder.getItems()) {
                if (item.getStatus() == ProductSnapshotStatus.REJECTED) {
                    item.setStatus(ProductSnapshotStatus.CANCELLED);
                }
            }
            return subOrderRepository.save(subOrder);
        }

        if (subOrder.getStatus() == SubOrderStatus.COMPLETED
                || subOrder.getStatus() == SubOrderStatus.RETURNED
                || subOrder.getStatus() == SubOrderStatus.PARTIALLY_RETURNED) {
            log.warn("[sub-order] Cancel skipped — already terminal: subOrderId={}, status={}", id, subOrder.getStatus());
            return subOrder;
        }

        boolean deliveryStarted = subOrder.getItems().stream()
                .anyMatch(item -> item.getStatus() != ProductSnapshotStatus.PENDING
                        && item.getStatus() != ProductSnapshotStatus.PACKING);
        if (deliveryStarted && !transactionTermination) {
            throw new IllegalStateException(
                    "Cannot cancel sub-order " + id + ": delivery has already started");
        }

        subOrder.setStatusReason(normalizeReason(reason, "Cancelled by the workflow."));
        subOrder.setStatus(SubOrderStatus.CANCELLED);
        for (ProductSnapshot item : subOrder.getItems()) {
            // Only restore stock for items that haven't been completed/returned/rejected yet
            if (item.getStatus() != ProductSnapshotStatus.COMPLETED
                    && item.getStatus() != ProductSnapshotStatus.RETURNED
                    && item.getStatus() != ProductSnapshotStatus.CANCELLED
                    && item.getStatus() != ProductSnapshotStatus.REJECTED) {
                item.setStatus(ProductSnapshotStatus.CANCELLED);
                productRepository.findById(item.getProductId()).ifPresent(product -> {
                    product.setQuantity(product.getQuantity() + item.getQuantity());
                    productRepository.save(product);
                });
            }
        }
        log.info("[sub-order] Cancelled and stock restored: subOrderId={}", id);

        return subOrderRepository.save(subOrder);
    }

    private String normalizeReason(String reason, String fallback) {
        return reason == null || reason.isBlank() ? fallback : reason.trim();
    }

    @Override
    @Transactional
    public SubOrder handoff(UUID id) {
        SubOrder subOrder = findById(id);
        for (ProductSnapshot item : subOrder.getItems()) {
            if (item.getStatus() == ProductSnapshotStatus.PACKING) {
                item.setStatus(ProductSnapshotStatus.DELIVERING);
            }
        }

        log.info("[sub-order] Handed to carrier; packed snapshots -> DELIVERING: subOrderId={}", id);
        return subOrderRepository.save(subOrder);
    }

    @Override
    @Transactional
    public SubOrder deliver(UUID id, UUID snapshotId) {
        SubOrder subOrder = findById(id);
        ProductSnapshot snapshot = subOrder.getItems().stream()
                .filter(item -> item.getId().equals(snapshotId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Product snapshot not found for ID: " + snapshotId));

        if (snapshot.getStatus() == ProductSnapshotStatus.DELIVERED_AWAITING_CONFIRMATION) {
            return subOrder;
        }
        if (snapshot.getStatus() != ProductSnapshotStatus.DELIVERING) {
            throw new IllegalStateException(
                    "Cannot finish delivery for snapshot " + snapshotId
                            + ": expected DELIVERING but was " + snapshot.getStatus());
        }

        // The domain transition also records deliveredAt when the snapshot first
        // reaches DELIVERED_AWAITING_CONFIRMATION.
        subOrder.updateSnapshotStatus(
                snapshotId, ProductSnapshotStatus.DELIVERED_AWAITING_CONFIRMATION);

        log.info("[sub-order] Snapshot delivered and awaiting confirmation: "
                        + "subOrderId={}, snapshotId={}, deliveredAt={}",
                id, snapshotId, snapshot.getDeliveredAt());
        return subOrderRepository.save(subOrder);
    }

    @Override
    @Transactional
    public SubOrder completeSubOrder(UUID id, SubOrderStatus status) {
        SubOrder subOrder = findById(id);
        if (status == null || status == SubOrderStatus.PENDING) {
            throw new IllegalArgumentException("A terminal sub-order status is required");
        }

        subOrder.setStatus(status);
        log.info("[sub-order] Terminal status written without changing snapshots: "
                + "subOrderId={}, status={}", id, status);
        return subOrderRepository.save(subOrder);
    }

}
