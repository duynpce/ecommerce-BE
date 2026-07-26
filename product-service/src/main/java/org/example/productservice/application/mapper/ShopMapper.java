package org.example.productservice.application.mapper;

import org.example.productservice.application.command.CreateShopCommand;
import org.example.productservice.application.command.UpdateShopCommand;
import org.example.productservice.application.criteria.ShopSearchCriteria;
import org.example.productservice.domain.model.Shop;
import org.example.productservice.infrastructure.web.data.entity.ShopEntity;
import org.example.productservice.infrastructure.web.dto.shop.CreateShopRequest;
import org.example.productservice.infrastructure.web.dto.shop.ShopFilter;
import org.example.productservice.infrastructure.web.dto.shop.ShopResponse;
import org.example.productservice.infrastructure.web.dto.shop.UpdateShopRequest;

import java.util.UUID;

public interface ShopMapper {
    Shop toDomain(ShopEntity entity);
    Shop toDomain(CreateShopCommand command);
    ShopEntity toEntity(Shop shop);
    void updateFromCommand(UpdateShopCommand command, Shop shop);

    CreateShopCommand toCommand(CreateShopRequest request, UUID contributorId);
    UpdateShopCommand toCommand(UpdateShopRequest request, UUID id, UUID senderId);

    ShopSearchCriteria toCriteria(ShopFilter filter);

    ShopResponse toResponse(Shop shop);
}
