package org.example.productservice.infrastructure.web.data.springdata;

import org.example.productservice.domain.constant.ShopStatus;
import org.example.productservice.infrastructure.web.data.entity.ShopEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataShopRepository extends MongoRepository<ShopEntity, UUID> {
    List<ShopEntity> findByContributorId(UUID contributorId);
    List<ShopEntity> findByStatus(ShopStatus status);
}
