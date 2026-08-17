package org.example.ticketservice.infrastructure.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateProductReviewRequest(
        @NotNull UUID productId,
        @NotNull UUID transactionId,
        @NotNull UUID snapshotId,
        @NotNull @Min(0) @Max(5) Integer rating,
        String comment
) {}
