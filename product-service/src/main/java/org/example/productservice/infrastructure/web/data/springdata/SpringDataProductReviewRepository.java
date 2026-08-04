package org.example.productservice.infrastructure.web.data.springdata;

import org.example.productservice.infrastructure.web.data.entity.ProductReviewEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataProductReviewRepository extends MongoRepository<ProductReviewEntity, UUID> {
    List<ProductReviewEntity> findAllByProductId(UUID productId);
    boolean existsByUserIdAndTransactionId(UUID userId, UUID transactionId);
}
