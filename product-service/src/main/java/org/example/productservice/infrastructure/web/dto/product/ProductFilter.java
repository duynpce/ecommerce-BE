package org.example.productservice.infrastructure.web.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.example.productservice.domain.constant.ProductCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Bound from query params on GET /products/search.
 * All filter fields are optional; page/limit have sane defaults.
 * Example: GET /products/search?name=shoes&category=FASHION&minPrice=10&page=0&limit=20
 */
public record ProductFilter(

        String name,
        ProductCategory category,
        UUID contributorId,

        @DecimalMin(value = "0.0", inclusive = true, message = "minPrice cannot be negative")
        BigDecimal minPrice,

        @DecimalMin(value = "0.0", inclusive = true, message = "maxPrice cannot be negative")
        BigDecimal maxPrice,

        LocalDate createdFrom,
        LocalDate createdTo,

        @NotNull(message = "page is required")
        @Min(value = 0, message = "page cannot be negative")
        Integer page,

        @NotNull(message = "limit is required")
        @Min(value = 1, message = "limit must be greater than 0")
        Integer limit
) {}
