package org.example.productservice.application.criteria;

import org.example.productservice.domain.constant.ShopStatus;

import java.util.UUID;

public record ShopSearchCriteria(
        String name,
        UUID contributorId,
        ShopStatus status,
        int page,
        int limit
) {}
