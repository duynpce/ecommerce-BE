package org.example.productservice.infrastructure.web.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.example.productservice.domain.constant.TransactionStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateTransactionRequest(

        @NotNull(message = "Transaction ID is required")
        UUID id,

        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,

        TransactionStatus status
) {}
