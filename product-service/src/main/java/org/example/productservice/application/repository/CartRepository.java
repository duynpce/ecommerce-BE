package org.example.productservice.application.repository;

import org.example.productservice.domain.model.Cart;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository {
    Cart save(Cart cart);
    Optional<Cart> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}
