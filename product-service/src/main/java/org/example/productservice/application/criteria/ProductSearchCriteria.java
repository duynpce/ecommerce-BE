package org.example.productservice.application.criteria;

import org.example.productservice.domain.constant.ProductCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Filter-only fields for product search. Pagination is handled via page/limit fields.
 * LocalDate from the filter is converted to Instant here for JPA comparison.
 */
public record ProductSearchCriteria(
        UUID shopId,
        String name,
        ProductCategory category,
        UUID contributorId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Instant createdFrom,
        Instant createdTo,
        Integer page,
        Integer limit
) {}
