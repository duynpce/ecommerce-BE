package org.example.productservice.infrastructure.mapper;

import org.example.productservice.application.command.CreateProductCommand;
import org.example.productservice.application.command.UpdateProductCommand;
import org.example.productservice.application.criteria.ProductSearchCriteria;
import org.example.productservice.application.mapper.ProductMapper;
import org.example.productservice.domain.constant.ProductStatus;
import org.example.productservice.domain.model.Product;
import org.example.productservice.infrastructure.web.data.entity.ProductEntity;
import org.example.productservice.infrastructure.web.dto.product.CreateProductRequest;
import org.example.productservice.infrastructure.web.dto.product.ProductFilter;
import org.example.productservice.infrastructure.web.dto.product.ProductResponse;
import org.example.productservice.infrastructure.web.dto.product.UpdateProductRequest;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = {ShopMapperMapstruct.class, DateMapper.class})
public interface ProductMapperMapstruct extends ProductMapper {

    @Override
    @Mapping(target = "shop", source = "shop")
    Product toDomain(ProductEntity entity);

    @Override
    Product toDomain(CreateProductCommand command);

    @Override
    @Mapping(target = "shop", ignore = true)
    ProductEntity toEntity(Product product);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromCommand(UpdateProductCommand command, @MappingTarget Product product);

    @Override
    @Mapping(target = "contributorId", source = "contributorId")
    CreateProductCommand toCommand(CreateProductRequest request, UUID contributorId);

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "senderId", source = "senderId")
    UpdateProductCommand toCommand(UpdateProductRequest request, UUID id, UUID senderId);

    @Override
    @Mapping(target = "createdFrom", source = "createdFrom", qualifiedByName = "localDateToInstantStart")
    @Mapping(target = "createdTo",   source = "createdTo",   qualifiedByName = "localDateToInstantEnd")
    @Mapping(target = "status", ignore = true)
    ProductSearchCriteria toCriteria(ProductFilter filter);

    @Override
    @Mapping(target = "createdFrom", source = "filter.createdFrom", qualifiedByName = "localDateToInstantStart")
    @Mapping(target = "createdTo",   source = "filter.createdTo",   qualifiedByName = "localDateToInstantEnd")
    ProductSearchCriteria toCriteria(ProductFilter filter, ProductStatus status);

    @Override
    ProductResponse toResponse(Product product);
}