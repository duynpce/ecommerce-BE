package org.example.productservice.infrastructure.web.data.adapter;

import lombok.RequiredArgsConstructor;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.criteria.ShopSearchCriteria;
import org.example.productservice.application.mapper.ShopMapper;
import org.example.productservice.application.repository.ShopRepository;
import org.example.productservice.domain.constant.ShopStatus;
import org.example.productservice.domain.model.Shop;
import org.example.productservice.infrastructure.web.data.entity.ShopEntity;
import org.example.productservice.infrastructure.web.data.specification.ShopSpecification;
import org.example.productservice.infrastructure.web.data.springdata.SpringDataShopRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ShopRepositoryAdapter implements ShopRepository {

    private final SpringDataShopRepository springDataRepo;
    private final ShopMapper shopMapper;
    private final MongoTemplate mongoTemplate;

    @Override
    public Shop save(Shop shop) {
        ShopEntity entity = shopMapper.toEntity(shop);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }

        return shopMapper.toDomain(springDataRepo.save(entity));
    }

    @Override
    public Optional<Shop> findById(UUID id) {
        return springDataRepo.findById(id).map(shopMapper::toDomain);
    }

    @Override
    public List<Shop> findByContributorId(UUID contributorId) {
        return springDataRepo.findByContributorId(contributorId).stream()
                .map(shopMapper::toDomain)
                .toList();
    }

    @Override
    public List<Shop> findByStatus(ShopStatus status) {
        return springDataRepo.findByStatus(status).stream()
                .map(shopMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataRepo.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepo.deleteById(id);
    }

    @Override
    public PageCommand<Shop> search(ShopSearchCriteria criteria) {
        Query query = ShopSpecification.fromCriteria(criteria);

        // Count before applying pagination so total reflects all matching documents
        long totalCount = mongoTemplate.count(query, ShopEntity.class);

        query.with(PageRequest.of(criteria.page(), criteria.limit()));
        List<Shop> shops = mongoTemplate.find(query, ShopEntity.class).stream()
                .map(shopMapper::toDomain)
                .toList();

        return PageCommand.of(shops, totalCount, criteria.page(), criteria.limit());
    }
}
