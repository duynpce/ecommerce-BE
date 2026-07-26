package org.example.productservice.application.repository;

import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.criteria.ShopSearchCriteria;
import org.example.productservice.domain.constant.ShopStatus;
import org.example.productservice.domain.model.Shop;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShopRepository {
    Shop save(Shop shop);
    Optional<Shop> findById(UUID id);
    List<Shop> findByContributorId(UUID contributorId);
    List<Shop> findByStatus(ShopStatus status);
    boolean existsById(UUID id);
    void deleteById(UUID id);
    PageCommand<Shop> search(ShopSearchCriteria criteria);
}
