package org.example.productservice.infrastructure.mapper;

import org.example.productservice.application.mapper.CartMapper;
import org.example.productservice.domain.model.Cart;
import org.example.productservice.domain.model.ProductSnapshot;
import org.example.productservice.infrastructure.web.data.entity.CartEntity;
import org.example.productservice.infrastructure.web.data.entity.ProductSnapshotEntity;
import org.mapstruct.Mapper;

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
}
