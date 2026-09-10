package org.example.productservice.application.usecase;

import org.example.productservice.application.command.CreateProductReviewCommand;
import org.example.productservice.application.command.UpdateProductReviewCommand;
import org.example.productservice.domain.model.ProductReview;

import java.util.List;
import java.util.UUID;

public interface ProductReviewUseCase {
    ProductReview create(CreateProductReviewCommand command);
    ProductReview findById(UUID id);
    List<ProductReview> findAllByProductId(UUID productId);
    ProductReview update(UpdateProductReviewCommand command);
}
