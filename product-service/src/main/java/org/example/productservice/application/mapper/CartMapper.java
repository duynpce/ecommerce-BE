package org.example.productservice.application.mapper;

import org.example.productservice.domain.model.Cart;
import org.example.productservice.domain.model.ProductSnapshot;
import org.example.productservice.infrastructure.web.data.entity.CartEntity;
import org.example.productservice.infrastructure.web.data.entity.ProductSnapshotEntity;

public interface CartMapper {
    Cart toDomain(CartEntity entity);
    CartEntity toEntity(Cart cart);

    ProductSnapshot toDomain(ProductSnapshotEntity entity);
    ProductSnapshotEntity toEntity(ProductSnapshot snapshot);
}
