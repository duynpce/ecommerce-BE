package org.example.productservice.application.usecase;

import org.example.productservice.application.command.CreateProductCommand;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateProductCommand;
import org.example.productservice.application.criteria.ProductSearchCriteria;
import org.example.productservice.domain.model.Product;

import java.util.UUID;

public interface ProductUseCase {
    Product create(CreateProductCommand command);
    Product findById(UUID id);
    Product update(UpdateProductCommand command);
    void delete(UUID id, String accessToken);
    PageCommand<Product> search(ProductSearchCriteria criteria);

    void censor(UUID id, Boolean isApproved);
}
