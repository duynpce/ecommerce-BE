package org.example.productservice.infrastructure.web.dto.shop;

import org.example.productservice.domain.constant.ShopStatus;
import org.springframework.web.multipart.MultipartFile;

/**
 * Multipart form request for updating a shop.
 * All fields are optional — only non-null values are applied by the mapper.
 */
public record UpdateShopRequest(

        MultipartFile logo,

        String name,

        String description,

        // ── pick-up address ────────────────────────────────────────────────
        String street,
        String ward,
        String district,
        String city,
        String country,
        String zipCode,

        ShopStatus status
) {}
