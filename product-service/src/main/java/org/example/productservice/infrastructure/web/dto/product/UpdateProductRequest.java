package org.example.productservice.infrastructure.web.dto.product;

import jakarta.validation.constraints.*;
import org.example.productservice.domain.constant.ProductCategory;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record UpdateProductRequest(
        @NotNull(message = "Product ID cannot be null")
        UUID id,

        String name,

        String description,

        List<MultipartFile> imgs,

        @DecimalMin(value = "0.0", message = "Price cannot be negative")
        BigDecimal price,

        @Min(value = 0, message = "Quantity cannot be negative")
        Integer quantity,

        ProductCategory category,

        Map<String, String> attributes
) {}

