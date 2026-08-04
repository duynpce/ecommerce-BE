package org.example.productservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.productservice.application.command.CreateProductReviewCommand;
import org.example.productservice.application.command.UpdateProductReviewCommand;
import org.example.productservice.application.mapper.ProductReviewMapper;
import org.example.productservice.application.repository.ProductRepository;
import org.example.productservice.application.repository.ProductReviewRepository;
import org.example.productservice.application.repository.ShopRepository;
import org.example.productservice.application.usecase.ProductReviewUseCase;
import org.example.productservice.domain.exception.ForbiddenException;
import org.example.productservice.domain.exception.NotFoundException;
import org.example.productservice.domain.model.Product;
import org.example.productservice.domain.model.ProductReview;
import org.example.productservice.domain.model.Shop;
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

    @Override
    @Transactional
    public ProductReview create(CreateProductReviewCommand command) {
        log.info("Creating review for productId: {} by userId: {}", command.productId(), command.userId());

        // Guard: product must exist
        Product product = productRepository.findByIdWithShop(command.productId())
                .orElseThrow(() -> new NotFoundException("Product not found: " + command.productId()));

        // Guard: one review per transaction
        if (productReviewRepository.existsByUserIdAndTransactionId(command.userId(), command.transactionId())) {
            throw new IllegalStateException("A review for this transaction already exists");
        }

        ProductReview review = productReviewMapper.toDomain(command);
        ProductReview saved = productReviewRepository.save(review);


        // Update product star counts and recalculate average rating
        incrementStarCount(product, saved.getRating());
        incrementStarCount(product.getShop(), saved.getRating());
        product.reCalculateRating();
        product.getShop().reCalculateRating();
        productRepository.save(product);
        shopRepository.save(product.getShop());

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
            Product product = productRepository.findById(review.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + review.getProductId()));

            decrementStarCount(product, oldRating);
            incrementStarCount(product, updated.getRating());
            product.reCalculateRating();
            productRepository.save(product);

            decrementStarCount(product.getShop(), oldRating);
            incrementStarCount(product.getShop(), updated.getRating());
            product.getShop().reCalculateRating();
            shopRepository.save(product.getShop());

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
