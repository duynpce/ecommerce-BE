package org.example.productservice.infrastructure.web.data.springdata;

import org.example.productservice.infrastructure.web.data.entity.TransactionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataTransactionRepository extends MongoRepository<TransactionEntity, UUID> {
    @Query("{ 'items.productId': ?0 }")
    List<TransactionEntity> findByProductId(UUID productId);

    default Optional<TransactionEntity> findByIdWithProductAndShop(UUID id) {
        return findById(id);
    }
}
