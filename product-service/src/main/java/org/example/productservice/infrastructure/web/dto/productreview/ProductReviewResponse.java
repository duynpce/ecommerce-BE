package org.example.productservice.infrastructure.web.dto.productreview;

import java.time.Instant;
import java.util.UUID;

public record ProductReviewResponse(
        UUID id,
        UUID productId,
        UUID userId,
        UUID transactionId,
        Integer rating,
        String comment,
        Instant createdAt,
        Instant updatedAt
) {}
