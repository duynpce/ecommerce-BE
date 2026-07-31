package org.example.productservice.infrastructure.web.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransactionRequest(

        @NotNull(message = "Product ID cannot be null")
        UUID productId,

        @NotNull(message = "Quantity cannot be null")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @NotNull(message = "Price cannot be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price
) {}
