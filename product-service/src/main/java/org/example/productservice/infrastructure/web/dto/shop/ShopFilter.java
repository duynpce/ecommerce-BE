package org.example.productservice.infrastructure.web.dto.shop;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.productservice.domain.constant.ShopStatus;

import java.util.UUID;

/**
 * Query parameters for shop search.
 * Example: GET /api/v1/shops/search?name=coffee&status=ACTIVE&page=0&limit=20
 */
public record ShopFilter(

        String name,

        UUID contributorId,

        ShopStatus status,

        @Min(0)
        int page,

        @Min(1) @Max(100)
        int limit
) {}
