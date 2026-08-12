package org.example.productservice.infrastructure.web.dto.suborder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateSubOrderRequest(
        @NotNull(message = "Transaction ID cannot be null")
        UUID transactionId,

        @NotNull(message = "Shop ID cannot be null")
        UUID shopId,

        @DecimalMin(value = "0.0", message = "Shipping fee cannot be negative")
        BigDecimal shippingFee,

        String note,

        @NotEmpty(message = "Items list cannot be empty")
        @Valid
        List<CreateSubOrderItemRequest> items
) {}
