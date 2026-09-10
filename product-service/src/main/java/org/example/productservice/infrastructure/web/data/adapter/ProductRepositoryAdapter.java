package org.example.productservice.infrastructure.web.data.adapter;

import lombok.RequiredArgsConstructor;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.criteria.ProductSearchCriteria;
import org.example.productservice.application.mapper.ProductMapper;
import org.example.productservice.application.repository.ProductRepository;
import org.example.productservice.domain.constant.ProductCategory;
import org.example.productservice.domain.model.Product;
import org.example.productservice.infrastructure.web.data.entity.ProductEntity;
import org.example.productservice.infrastructure.web.data.specification.ProductSpecification;
import org.example.productservice.infrastructure.web.data.springdata.SpringDataProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final SpringDataProductRepository springDataRepo;
    private final ProductMapper productMapper;
    // MongoTemplate replaces JpaSpecificationExecutor for dynamic queries
    private final MongoTemplate mongoTemplate;

    @Override
    public Product save(Product product) {
        ProductEntity entity = productMapper.toEntity(product);
        // MongoDB has no @GeneratedValue — assign a UUID for new documents
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        return productMapper.toDomain(springDataRepo.save(entity));
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return springDataRepo.findById(id).map(productMapper::toDomain);
    }

    @Override
    public List<Product> findByContributorId(UUID contributorId) {
        return springDataRepo.findByContributorId(contributorId).stream()
                .map(productMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Product> findByIdWithShop(UUID productId) {
        return springDataRepo.findByIdWithShop(productId)
                .map(productMapper::toDomain);
    }

    @Override
    public List<Product> findByCategory(ProductCategory category) {
        return springDataRepo.findByCategory(category).stream()
                .map(productMapper::toDomain)
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
    public PageCommand<Product> search(ProductSearchCriteria criteria) {
        Query query = ProductSpecification.fromCriteria(criteria);

        // Count before applying pagination so the total reflects all matching documents
        long totalCount = mongoTemplate.count(query, ProductEntity.class);

        query.with(PageRequest.of(criteria.page(), criteria.limit()));
        List<Product> products = mongoTemplate.find(query, ProductEntity.class).stream()
                .map(productMapper::toDomain)
                .toList();

        return PageCommand.of(products, totalCount, criteria.page(), criteria.limit());
    }
}
