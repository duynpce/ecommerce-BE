package org.example.ticketservice.infrastructure.product.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSnapshotDto(
        UUID id,
        UUID productId,
        String name,
        BigDecimal price,
        Integer quantity,
        String imageUrl,
        String status,
        Boolean isReviewed
) {}
