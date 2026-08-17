package org.example.productservice.infrastructure.web.dto.suborder;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateSubOrderRequest(
        @DecimalMin(value = "0.0", message = "Shipping fee cannot be negative")
        BigDecimal shippingFee,

        String note
) {}
