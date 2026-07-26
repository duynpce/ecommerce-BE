package org.example.productservice.application.command;

import org.example.productservice.domain.constant.ProductCategory;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CreateProductCommand(
        UUID shopId,
        UUID contributorId,
        List<MultipartFile> imgs,
        String name,
        String description,
        BigDecimal price,
        Integer quantity,
        ProductCategory category,
        Map<String, String> attributes
) {
    void validate() {
        if (shopId == null) {
            throw new IllegalArgumentException("Shop ID cannot be null");
        }
        if (contributorId == null) {
            throw new IllegalArgumentException("Contributor ID cannot be null");
        }
        if (imgs == null || imgs.isEmpty()) {
            throw new IllegalArgumentException("At least one product image is required");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity cannot be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
    }
}
