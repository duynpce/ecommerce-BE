package org.example.productservice.infrastructure.web.dto.shop;

public record AddressResponse(
        String street,
        String ward,
        String district,
        String city,
        String country,
        String zipCode
) {}
