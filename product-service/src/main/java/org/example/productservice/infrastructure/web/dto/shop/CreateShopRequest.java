package org.example.productservice.infrastructure.web.dto.shop;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

/**
 * Multipart form request for creating a shop.
 * Address fields are flattened for {@code @ModelAttribute} binding compatibility.
 */
public record CreateShopRequest(

        MultipartFile logo,

        @NotBlank(message = "Shop name cannot be blank")
        String name,

        String description,

        // ── pick-up address (all optional) ─────────────────────────────────
        String street,
        String ward,
        String district,
        String city,
        String country,
        String zipCode
) {}
