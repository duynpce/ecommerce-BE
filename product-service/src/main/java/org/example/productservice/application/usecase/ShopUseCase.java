package org.example.productservice.application.usecase;

import org.example.productservice.application.command.CreateShopCommand;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateShopCommand;
import org.example.productservice.application.criteria.ShopSearchCriteria;
import org.example.productservice.domain.model.Shop;

import java.util.UUID;

public interface ShopUseCase {
    Shop create(CreateShopCommand command);
    Shop findById(UUID id);
    Shop update(UpdateShopCommand command);
    void delete(UUID id, String accessToken);
    PageCommand<Shop> search(ShopSearchCriteria criteria);
}
