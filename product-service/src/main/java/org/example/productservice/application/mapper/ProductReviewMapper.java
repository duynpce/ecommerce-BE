package org.example.productservice.application.mapper;

import org.example.productservice.application.command.CreateProductReviewCommand;
import org.example.productservice.application.command.UpdateProductReviewCommand;
import org.example.productservice.domain.model.ProductReview;
import org.example.productservice.infrastructure.web.data.entity.ProductReviewEntity;
import org.example.productservice.infrastructure.web.dto.productreview.CreateProductReviewRequest;
import org.example.productservice.infrastructure.web.dto.productreview.ProductReviewResponse;
import org.example.productservice.infrastructure.web.dto.productreview.UpdateProductReviewRequest;

import java.util.UUID;

public interface ProductReviewMapper {
    ProductReview toDomain(ProductReviewEntity entity);
    ProductReview toDomain(CreateProductReviewCommand command);
    ProductReviewEntity toEntity(ProductReview productReview);
    void updateFromCommand(UpdateProductReviewCommand command, ProductReview productReview);

    CreateProductReviewCommand toCommand(CreateProductReviewRequest request, UUID userId);
    UpdateProductReviewCommand toCommand(UpdateProductReviewRequest request, UUID id, UUID senderId);

    ProductReviewResponse toResponse(ProductReview productReview);
}
