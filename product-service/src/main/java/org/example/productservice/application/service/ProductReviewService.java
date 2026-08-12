package org.example.productservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.productservice.application.command.CreateProductReviewCommand;
import org.example.productservice.application.command.UpdateProductReviewCommand;
import org.example.productservice.application.mapper.ProductReviewMapper;
import org.example.productservice.application.repository.ProductRepository;
import org.example.productservice.application.repository.ProductReviewRepository;
import org.example.productservice.application.repository.ShopRepository;
import org.example.productservice.application.repository.SubOrderRepository;
import org.example.productservice.application.usecase.ProductReviewUseCase;
import org.example.productservice.domain.exception.ForbiddenException;
import org.example.productservice.domain.exception.NotFoundException;
import org.example.productservice.domain.model.Product;
import org.example.productservice.domain.model.ProductReview;
import org.example.productservice.domain.model.Shop;
import org.example.productservice.domain.model.SubOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReviewService implements ProductReviewUseCase {

    private final ProductReviewRepository productReviewRepository;
    private final ProductRepository productRepository;
    private final ProductReviewMapper productReviewMapper;
    private final ShopRepository shopRepository;
    private final SubOrderRepository subOrderRepository;

    @Override
    @Transactional
    public ProductReview create(CreateProductReviewCommand command) {
        log.info("Creating review for productId: {} by userId: {}", command.productId(), command.userId());

        SubOrder subOrder = subOrderRepository.findByTransactionId(command.transactionId()).stream()
                .filter(so -> so.getItems().stream().anyMatch(item -> item.getId().equals(command.snapshotId())))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Sub-order not found for transaction: " + command.transactionId() + " and snapshot: " + command.snapshotId()));

        if (subOrder.getSnapshotDeliveredAt(command.snapshotId()) == null) {
            throw new IllegalArgumentException(String.format("Snapshot %s has not been delivered for transaction %s", command.snapshotId(), command.transactionId()));
        }

        boolean productInSubOrder = subOrder.getItems().stream()
                .anyMatch(item -> item.getProductId().equals(command.productId()));

        if (!productInSubOrder) {
            throw new IllegalArgumentException(String.format("Product %s is not part of sub-order %s", command.productId(), subOrder.getId()));
        }

        Product product = productRepository.findByIdWithShop(command.productId())
                .orElseThrow(() -> new NotFoundException("Product not found: " + command.productId()));

        Shop shop = product.getShop();
        if (shop == null) {
            throw new NotFoundException("Shop not found for product: " + command.productId());
        }

        if (Boolean.TRUE.equals(subOrder.getIsReviewedBySnapshotId(command.snapshotId()))) {
            throw new IllegalArgumentException("Snapshot has already been reviewed: " + command.snapshotId());
        }

        subOrder.setIsReviewedBySnapshotId(command.snapshotId(), true);
        subOrderRepository.save(subOrder);

        ProductReview review = productReviewMapper.toDomain(command);
        ProductReview saved = productReviewRepository.save(review);

        incrementStarCount(product, saved.getRating());
        product.reCalculateRating();
        productRepository.save(product);

        incrementStarCount(shop, saved.getRating());
        shop.reCalculateRating();
        shopRepository.save(shop);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductReview findById(UUID id) {
        return productReviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductReview> findAllByProductId(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found: " + productId);
        }
        return productReviewRepository.findAllByProductId(productId);
    }

    @Override
    @Transactional
    public ProductReview update(UpdateProductReviewCommand command) {
        log.info("Updating review id: {} by userId: {}", command.id(), command.senderId());

        ProductReview review = findById(command.id());

        if (!review.getUserId().equals(command.senderId())) {
            throw new ForbiddenException("You are not the owner of this review");
        }

        int oldRating = review.getRating();
        productReviewMapper.updateFromCommand(command, review);
        ProductReview updated = productReviewRepository.save(review);

        // Recalculate product star counts if rating changed
        if (command.rating() != null && command.rating() != oldRating) {
            Product product = productRepository.findByIdWithShop(review.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + review.getProductId()));

            decrementStarCount(product, oldRating);
            incrementStarCount(product, updated.getRating());
            product.reCalculateRating();
            productRepository.save(product);

            if (product.getShop() != null) {
                decrementStarCount(product.getShop(), oldRating);
                incrementStarCount(product.getShop(), updated.getRating());
                product.getShop().reCalculateRating();
                shopRepository.save(product.getShop());
            }
        }

        return updated;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void incrementStarCount(Product product, int rating) {
        applyStarDelta(product, rating, +1);
    }

    private void decrementStarCount(Product product, int rating) {
        applyStarDelta(product, rating, -1);
    }

    private void applyStarDelta(Product product, int rating, int delta) {
        switch (rating) {
            case 1 -> product.setOneStarRatingCount(Math.max(0, orZero(product.getOneStarRatingCount()) + delta));
            case 2 -> product.setTwoStarRatingCount(Math.max(0, orZero(product.getTwoStarRatingCount()) + delta));
            case 3 -> product.setThreeStarRatingCount(Math.max(0, orZero(product.getThreeStarRatingCount()) + delta));
            case 4 -> product.setFourStarRatingCount(Math.max(0, orZero(product.getFourStarRatingCount()) + delta));
            case 5 -> product.setFiveStarRatingCount(Math.max(0, orZero(product.getFiveStarRatingCount()) + delta));
        }
    }

    private void incrementStarCount(Shop shop, int rating) {
        applyStarDelta(shop, rating, +1);
    }

    private void decrementStarCount(Shop shop, int rating) {
        applyStarDelta(shop, rating, -1);
    }

    private void applyStarDelta(Shop shop, int rating, int delta) {
        switch (rating) {
            case 1 -> shop.setOneStarRatingCount(Math.max(0, orZero(shop.getOneStarRatingCount()) + delta));
            case 2 -> shop.setTwoStarRatingCount(Math.max(0, orZero(shop.getTwoStarRatingCount()) + delta));
            case 3 -> shop.setThreeStarRatingCount(Math.max(0, orZero(shop.getThreeStarRatingCount()) + delta));
            case 4 -> shop.setFourStarRatingCount(Math.max(0, orZero(shop.getFourStarRatingCount()) + delta));
            case 5 -> shop.setFiveStarRatingCount(Math.max(0, orZero(shop.getFiveStarRatingCount()) + delta));
        }
    }

    private int orZero(Integer value) {
        return value == null ? 0 : value;
    }
}
