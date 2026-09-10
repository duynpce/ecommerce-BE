package org.example.productservice.application.mapper;

import org.example.productservice.application.command.CreateProductCommand;
import org.example.productservice.application.command.UpdateProductCommand;
import org.example.productservice.application.criteria.ProductSearchCriteria;
import org.example.productservice.domain.constant.ProductStatus;
import org.example.productservice.domain.model.Product;
import org.example.productservice.infrastructure.web.data.entity.ProductEntity;
import org.example.productservice.infrastructure.web.dto.product.CreateProductRequest;
import org.example.productservice.infrastructure.web.dto.product.ProductFilter;
import org.example.productservice.infrastructure.web.dto.product.ProductResponse;
import org.example.productservice.infrastructure.web.dto.product.UpdateProductRequest;

import java.util.UUID;

public interface ProductMapper {
    Product toDomain(ProductEntity entity);
    Product toDomain(CreateProductCommand command);
    ProductEntity toEntity(Product product);
    void updateFromCommand(UpdateProductCommand command, Product product);

    CreateProductCommand toCommand(CreateProductRequest request, UUID contributorId);
    UpdateProductCommand toCommand(UpdateProductRequest request, UUID id, UUID senderId);

    ProductSearchCriteria toCriteria(ProductFilter filter);
    ProductSearchCriteria toCriteria(ProductFilter filter, ProductStatus status);

    ProductResponse toResponse(Product product);
}
