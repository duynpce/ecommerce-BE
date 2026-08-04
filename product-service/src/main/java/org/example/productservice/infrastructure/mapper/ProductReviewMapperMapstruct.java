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

@Mapper(componentModel = "spring")
public interface ProductReviewMapperMapstruct extends ProductReviewMapper {

    @Override
    ProductReview toDomain(ProductReviewEntity entity);

    @Override
    ProductReview toDomain(CreateProductReviewCommand command);

    @Override
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
