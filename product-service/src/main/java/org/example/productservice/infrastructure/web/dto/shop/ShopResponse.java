package org.example.productservice.infrastructure.web.dto.shop;

import org.example.productservice.domain.constant.ShopStatus;

import java.time.Instant;
import java.util.UUID;

public record ShopResponse(
        UUID id,
        UUID contributorId,
        String name,
        String description,
        String logoUrl,
        AddressResponse pickUpAddress,
        ShopStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
