package org.example.productservice.infrastructure.mapper;

import org.example.productservice.application.command.CreateProductCommand;
import org.example.productservice.application.command.UpdateProductCommand;
import org.example.productservice.application.criteria.ProductSearchCriteria;
import org.example.productservice.application.mapper.ProductMapper;
import org.example.productservice.domain.model.Product;
import org.example.productservice.infrastructure.web.data.entity.ProductEntity;
import org.example.productservice.infrastructure.web.dto.product.CreateProductRequest;
import org.example.productservice.infrastructure.web.dto.product.ProductFilter;
import org.example.productservice.infrastructure.web.dto.product.ProductResponse;
import org.example.productservice.infrastructure.web.dto.product.UpdateProductRequest;
import org.mapstruct.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProductMapperMapstruct extends ProductMapper {

    @Override
    Product toDomain(ProductEntity entity);

    @Override
    Product toDomain(CreateProductCommand command);

    @Override
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
    @Mapping(target = "createdTo", source = "createdTo", qualifiedByName = "localDateToInstantEnd")
    ProductSearchCriteria toCriteria(ProductFilter filter);

    @Override
    ProductResponse toResponse(Product product);

    @Named("localDateToInstantStart")
    default Instant localDateToInstantStart(LocalDate date) {
        return date == null ? null : date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    @Named("localDateToInstantEnd")
    default Instant localDateToInstantEnd(LocalDate date) {
        return date == null ? null : date.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC).toInstant();
    }
}
