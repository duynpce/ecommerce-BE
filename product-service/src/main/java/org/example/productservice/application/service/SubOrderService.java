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
import org.example.productservice.application.usecase.SubOrderUseCase;
import org.example.productservice.domain.constant.ProductSnapshotStatus;
import org.example.productservice.domain.constant.ProductStatus;
import org.example.productservice.domain.constant.SubOrderStatus;
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

        subOrder.setStatus(newStatus);

        // If sub-order is cancelled or returned, restore product stock
        if (newStatus == SubOrderStatus.CANCELLED || newStatus == SubOrderStatus.RETURNED) {
            for (ProductSnapshot item : subOrder.getItems()) {
                productRepository.findById(item.getProductId()).ifPresent(product -> {
                    product.setQuantity(product.getQuantity() + item.getQuantity());
                    productRepository.save(product);
                });
            }
        }

        return subOrderRepository.save(subOrder);
    }

    @Override
    @Transactional
    public SubOrder updateSnapshotStatus(UUID subOrderId, UUID snapshotId, ProductSnapshotStatus newStatus) {
        SubOrder subOrder = findById(subOrderId);
        subOrder.updateSnapshotStatus(snapshotId, newStatus);

        // If item status becomes RETURNED, restore stock for that item
        if (newStatus == ProductSnapshotStatus.RETURNED) {
            subOrder.getItems().stream()
                    .filter(i -> i.getId().equals(snapshotId))
                    .findFirst()
                    .ifPresent(item -> productRepository.findById(item.getProductId()).ifPresent(product -> {
                        product.setQuantity(product.getQuantity() + item.getQuantity());
                        productRepository.save(product);
                    }));
        } else if (newStatus == ProductSnapshotStatus.COMPLETED) {
            // Increment sold count
            subOrder.getItems().stream()
                    .filter(i -> i.getId().equals(snapshotId))
                    .findFirst()
                    .ifPresent(item -> productRepository.findByIdWithShop(item.getProductId()).ifPresent(product -> {
                        product.incrementSoldQuantity(item.getQuantity());
                        if (product.getShop() != null) {
                            Shop shop = product.getShop();
                            shop.incrementSoldQuantity(item.getQuantity());
                            shopRepository.save(shop);
                        }
                        productRepository.save(product);
                    }));
        }

        return subOrderRepository.save(subOrder);
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
}
