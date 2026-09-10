package org.example.productservice.infrastructure.web.dto.productreview;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateProductReviewRequest(

        @Min(value = 0, message = "Rating cannot be less than 0")
        @Max(value = 5, message = "Rating cannot be more than 5")
        Integer rating,

        String comment
) {}
