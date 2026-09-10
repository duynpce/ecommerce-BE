package org.example.productservice.application.repository;

import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.criteria.ProductSearchCriteria;
import org.example.productservice.domain.constant.ProductCategory;
import org.example.productservice.domain.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID id);
    Optional<Product> findByIdWithShop(UUID productId);
    List<Product> findByContributorId(UUID contributorId);
    List<Product> findByCategory(ProductCategory category);
    boolean existsById(UUID id);
    void deleteById(UUID id);
    PageCommand<Product> search(ProductSearchCriteria criteria);
}
