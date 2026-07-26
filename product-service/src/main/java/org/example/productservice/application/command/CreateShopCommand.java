package org.example.productservice.application.command;

import org.example.productservice.domain.valueobject.Address;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record CreateShopCommand(
        UUID contributorId,
        MultipartFile logo,
        String name,
        String description,
        Address pickUpAddress
) {
    void validate() {
        if (contributorId == null) {
            throw new IllegalArgumentException("Contributor ID cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Shop name cannot be null or empty");
        }
    }
}
