package org.example.productservice.application.command;

import org.example.productservice.domain.constant.ProductCategory;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record UpdateProductCommand(
        UUID id,
        UUID senderId,
        List<MultipartFile> imgs,
        String name,
        String description,
        BigDecimal price,
        Integer quantity,
        ProductCategory category,
        Map<String, String> attributes
) {}

