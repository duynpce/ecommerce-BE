package org.example.productservice.application.command;

import java.util.UUID;

public record CreateProductReviewCommand(
        UUID productId,
        UUID userId,
        UUID transactionId,
        Integer rating,
        String comment
) {}
