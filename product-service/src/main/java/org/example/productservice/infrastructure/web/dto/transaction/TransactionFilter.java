package org.example.productservice.infrastructure.web.dto.transaction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.example.productservice.domain.constant.TransactionStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Bound from query params on GET /transactions/search and /transactions/admin/search.
 * All filter fields are optional; page/limit are required.
 * Example: GET /transactions/search?status=PENDING&createdFrom=2026-06-01&page=0&limit=20
 */
public record TransactionFilter(

        UUID productId,
        TransactionStatus status,
        LocalDate createdFrom,
        LocalDate createdTo,

        @NotNull(message = "page is required")
        @Min(value = 0, message = "page cannot be negative")
        Integer page,

        @NotNull(message = "limit is required")
        @Min(value = 1, message = "limit must be greater than 0")
        Integer limit
) {}
