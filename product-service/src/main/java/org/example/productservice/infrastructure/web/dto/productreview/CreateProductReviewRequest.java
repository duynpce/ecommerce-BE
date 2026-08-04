package org.example.productservice.infrastructure.web.dto.productreview;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record CreateProductReviewRequest(

        @NotNull(message = "Product ID cannot be null")
        UUID productId,

        @NotNull(message = "Transaction ID cannot be null")
        UUID transactionId,

        @NotNull(message = "Rating cannot be null")
        @Min(value = 0, message = "Rating cannot be less than 0")
        @Max(value = 5, message = "Rating cannot be more than 5")
        Integer rating,

        String comment
) {}
