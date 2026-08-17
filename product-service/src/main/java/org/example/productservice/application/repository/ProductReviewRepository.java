package org.example.productservice.application.repository;

import org.example.productservice.domain.model.ProductReview;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductReviewRepository {
    ProductReview save(ProductReview productReview);
    Optional<ProductReview> findById(UUID id);
    List<ProductReview> findAllByProductId(UUID productId);
    boolean existsByUserIdAndTransactionId(UUID userId, UUID transactionId);
    void deleteById(UUID id);
}
