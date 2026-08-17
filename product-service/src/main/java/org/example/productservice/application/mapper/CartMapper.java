package org.example.productservice.application.mapper;

import org.example.productservice.application.command.AddToCartCommand;
import org.example.productservice.application.command.UpdateCartItemCommand;
import org.example.productservice.domain.model.Cart;
import org.example.productservice.domain.model.ProductSnapshot;
import org.example.productservice.infrastructure.web.data.entity.CartEntity;
import org.example.productservice.infrastructure.web.data.entity.ProductSnapshotEntity;
import org.example.productservice.infrastructure.web.dto.cart.AddToCartRequest;
import org.example.productservice.infrastructure.web.dto.cart.CartItemResponse;
import org.example.productservice.infrastructure.web.dto.cart.CartResponse;
import org.example.productservice.infrastructure.web.dto.cart.UpdateCartItemRequest;

import java.util.UUID;

public interface CartMapper {
    Cart toDomain(CartEntity entity);
    CartEntity toEntity(Cart cart);

    ProductSnapshot toDomain(ProductSnapshotEntity entity);
    ProductSnapshotEntity toEntity(ProductSnapshot snapshot);

    // ── Web-layer mapping ─────────────────────────────────────────────────────

    AddToCartCommand toCommand(AddToCartRequest request, UUID userId);
    UpdateCartItemCommand toCommand(UpdateCartItemRequest request, UUID userId, UUID productId);

    CartItemResponse toItemResponse(ProductSnapshot snapshot);
    CartResponse toResponse(Cart cart);
}
