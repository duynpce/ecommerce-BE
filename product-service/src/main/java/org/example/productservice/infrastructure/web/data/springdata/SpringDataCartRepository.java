package org.example.productservice.infrastructure.web.data.springdata;

import org.example.productservice.infrastructure.web.data.entity.CartEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataCartRepository extends MongoRepository<CartEntity, UUID> {
    Optional<CartEntity> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}
