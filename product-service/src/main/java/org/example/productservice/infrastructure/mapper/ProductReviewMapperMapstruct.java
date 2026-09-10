package org.example.productservice.infrastructure.mapper;

import org.example.productservice.application.command.CreateProductReviewCommand;
import org.example.productservice.application.command.UpdateProductReviewCommand;
import org.example.productservice.application.mapper.ProductReviewMapper;
import org.example.productservice.domain.model.ProductReview;
import org.example.productservice.infrastructure.web.data.entity.ProductReviewEntity;
import org.example.productservice.infrastructure.web.dto.productreview.CreateProductReviewRequest;
import org.example.productservice.infrastructure.web.dto.productreview.ProductReviewResponse;
import org.example.productservice.infrastructure.web.dto.productreview.UpdateProductReviewRequest;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = {ProductMapperMapstruct.class, TransactionMapperMapStruct.class})
public interface ProductReviewMapperMapstruct extends ProductReviewMapper {

    @Override
    @Mapping(target = "product", source = "product")         // ProductMapperMapstruct.toDomain(ProductEntity) — includes nested shop
    @Mapping(target = "transaction", source = "transaction")  // TransactionMapperMapStruct.toDomain(TransactionEntity) — includes nested product+shop
    ProductReview toDomain(ProductReviewEntity entity);

    @Override
    @Mapping(target = "product",     ignore = true)
    @Mapping(target = "transaction", ignore = true)
    ProductReview toDomain(CreateProductReviewCommand command);

    @Override
    @Mapping(target = "product", ignore = true)      // @Transient — never persisted
    @Mapping(target = "transaction", ignore = true)  // @Transient — never persisted
    ProductReviewEntity toEntity(ProductReview productReview);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromCommand(UpdateProductReviewCommand command, @MappingTarget ProductReview productReview);

    @Override
    @Mapping(target = "userId", source = "userId")
    CreateProductReviewCommand toCommand(CreateProductReviewRequest request, UUID userId);

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "senderId", source = "senderId")
    UpdateProductReviewCommand toCommand(UpdateProductReviewRequest request, UUID id, UUID senderId);

    @Override
    ProductReviewResponse toResponse(ProductReview productReview);
}