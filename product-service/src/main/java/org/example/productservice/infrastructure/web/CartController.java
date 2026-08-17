package org.example.productservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.productservice.application.client.TokenGeneratorClient;
import org.example.productservice.application.command.AddToCartCommand;
import org.example.productservice.application.command.UpdateCartItemCommand;
import org.example.productservice.application.mapper.CartMapper;
import org.example.productservice.application.usecase.CartUseCase;
import org.example.productservice.domain.model.Cart;
import org.example.productservice.infrastructure.web.dto.ResponseDto;
import org.example.productservice.infrastructure.web.dto.cart.AddToCartRequest;
import org.example.productservice.infrastructure.web.dto.cart.CartResponse;
import org.example.productservice.infrastructure.web.dto.cart.UpdateCartItemRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing cart operations for the currently authenticated user.
 *
 * <pre>
 *   GET    /carts/me              – view your cart
 *   POST   /carts/me/items        – add an item (merges quantity if already present)
 *   PUT    /carts/me/items/{productId}  – update item quantity (0 removes the item)
 *   DELETE /carts/me/items/{productId}  – remove a specific item
 *   DELETE /carts/me             – clear the whole cart
 * </pre>
 */
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartUseCase cartUseCase;
    private final CartMapper cartMapper;
    private final TokenGeneratorClient tokenGeneratorClient;

    // ── Read ───────────────────────────────────────────────────────────────────

    /**
     * Returns the caller's cart (creates an empty one on first access).
     * <p>GET /carts/me</p>
     */
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('CART:READ_SELF')")
    public ResponseEntity<ResponseDto<CartResponse>> getMyCart(
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UUID userId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        Cart cart = cartUseCase.getCart(userId);
        CartResponse response = cartMapper.toResponse(cart);
        return ResponseEntity.ok(ResponseDto.success(response));
    }

    // ── Write ──────────────────────────────────────────────────────────────────

    /**
     * Adds a product to the cart, merging quantity if already present.
     * <p>POST /carts/me/items</p>
     */
    @PostMapping("/me/items")
    @PreAuthorize("hasAuthority('CART:WRITE_SELF')")
    public ResponseEntity<ResponseDto<CartResponse>> addItem(
            @Valid @RequestBody AddToCartRequest request,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UUID userId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        AddToCartCommand command = cartMapper.toCommand(request, userId);
        Cart cart = cartUseCase.addItem(command);
        return ResponseEntity.ok(ResponseDto.success(cartMapper.toResponse(cart), "Item added to cart"));
    }

    /**
     * Updates the quantity of an existing cart item.
     * Sending {@code quantity = 0} removes the item entirely.
     * <p>PUT /carts/me/items/{productId}</p>
     */
    @PutMapping("/me/items/{productId}")
    @PreAuthorize("hasAuthority('CART:WRITE_SELF')")
    public ResponseEntity<ResponseDto<CartResponse>> updateItem(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UUID userId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        UpdateCartItemCommand command = cartMapper.toCommand(request, userId, productId);
        Cart cart = cartUseCase.updateItem(command);
        return ResponseEntity.ok(ResponseDto.success(cartMapper.toResponse(cart), "Cart item updated"));
    }

    /**
     * Removes a specific product from the cart.
     * <p>DELETE /carts/me/items/{productId}</p>
     */
    @DeleteMapping("/me/items")
    @PreAuthorize("hasAuthority('CART:WRITE_SELF')")
    public ResponseEntity<ResponseDto<CartResponse>> removeItem(
            @RequestBody List<UUID> productIds,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UUID userId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        Cart cart = cartUseCase.removeItem(userId, productIds);
        return ResponseEntity.ok(ResponseDto.success(cartMapper.toResponse(cart), "Item(s) removed from cart"));
    }


    /**
     * Clears all items from the cart.
     * <p>DELETE /carts/me</p>
     */
    @DeleteMapping("/me")
    @PreAuthorize("hasAuthority('CART:WRITE_SELF')")
    public ResponseEntity<Void> clearCart(
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UUID userId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        cartUseCase.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
