package org.example.productservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.productservice.application.command.AddToCartCommand;
import org.example.productservice.application.command.UpdateCartItemCommand;
import org.example.productservice.application.repository.CartRepository;
import org.example.productservice.application.repository.ProductRepository;
import org.example.productservice.application.usecase.CartUseCase;
import org.example.productservice.domain.exception.NotFoundException;
import org.example.productservice.domain.model.Cart;
import org.example.productservice.domain.model.Product;
import org.example.productservice.domain.model.ProductSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService implements CartUseCase {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** Fetches the cart for the user; creates and persists an empty one if absent. */
    private Cart getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart(UUID.randomUUID(), userId);
                    return cartRepository.save(newCart);
                });
    }

    // ── CartUseCase ────────────────────────────────────────────────────────────

    @Override
    public Cart getCart(UUID userId) {
        return getOrCreateCart(userId);
    }

    @Override
    @Transactional
    public Cart addItem(AddToCartCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new NotFoundException("Product not found: " + command.productId()));

        log.info("Adding product {} (qty={}) to cart of user {}", command.productId(), command.quantity(), command.userId());

        Cart cart = getOrCreateCart(command.userId());
        cart.addItem(ProductSnapshot.of(product, command.quantity()));
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart updateItem(UpdateCartItemCommand command) {
        Cart cart = cartRepository.findByUserId(command.userId())
                .orElseThrow(() -> new NotFoundException("Cart not found for user: " + command.userId()));

        log.info("Updating product {} to qty={} in cart of user {}", command.productId(), command.quantity(), command.userId());

        cart.updateItemQuantity(command.productId(), command.quantity());
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart removeItem(UUID userId, List<UUID> productIds) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found for user: " + userId));

        log.info("Removing products {} from cart of user {}", productIds, userId);

        boolean removed = cart.removeItems(productIds);
        if (!removed) {
            throw new NotFoundException("None of the products were found in cart: " + productIds);
        }
        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found for user: " + userId));

        log.info("Clearing cart of user {}", userId);

        cart.clear();
        cartRepository.save(cart);
    }
}
