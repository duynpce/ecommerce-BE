package org.example.productservice.infrastructure.mapper;

import org.example.productservice.application.command.AddToCartCommand;
import org.example.productservice.application.command.UpdateCartItemCommand;
import org.example.productservice.application.mapper.CartMapper;
import org.example.productservice.domain.model.Cart;
import org.example.productservice.domain.model.ProductSnapshot;
import org.example.productservice.infrastructure.web.data.entity.CartEntity;
import org.example.productservice.infrastructure.web.data.entity.ProductSnapshotEntity;
import org.example.productservice.infrastructure.web.dto.cart.AddToCartRequest;
import org.example.productservice.infrastructure.web.dto.cart.CartItemResponse;
import org.example.productservice.infrastructure.web.dto.cart.CartResponse;
import org.example.productservice.infrastructure.web.dto.cart.UpdateCartItemRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CartMapperMapstruct extends CartMapper {

    @Override
    Cart toDomain(CartEntity entity);

    @Override
    CartEntity toEntity(Cart cart);

    @Override
    ProductSnapshot toDomain(ProductSnapshotEntity entity);

    @Override
    ProductSnapshotEntity toEntity(ProductSnapshot snapshot);

    // ── Web-layer mapping ─────────────────────────────────────────────────────

    @Override
    default AddToCartCommand toCommand(AddToCartRequest request, UUID userId) {
        return new AddToCartCommand(userId, request.productId(), request.quantity());
}
    @Override
    default UpdateCartItemCommand toCommand(UpdateCartItemRequest request, UUID userId, UUID productId) {
        return new UpdateCartItemCommand(userId, productId, request.quantity());
    }

    @Override
    @Mapping(target = "subtotal", expression = "java(snapshot.subtotal())")
    CartItemResponse toItemResponse(ProductSnapshot snapshot);

    @Override
    default CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toItemResponse)
                .toList();
        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(items)
                .totalAmount(cart.calculateTotal())
                .build();
    }
}
