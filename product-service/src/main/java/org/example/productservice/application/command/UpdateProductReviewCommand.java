package org.example.productservice.application.command;

import java.util.UUID;

public record UpdateProductReviewCommand(
        UUID id,
        UUID senderId,
        Integer rating,
        String comment
) {}
