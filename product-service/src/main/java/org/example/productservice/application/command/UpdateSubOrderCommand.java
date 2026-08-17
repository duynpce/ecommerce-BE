package org.example.productservice.application.command;

import org.example.productservice.domain.constant.SubOrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateSubOrderCommand(
        UUID id,
        BigDecimal shippingFee,
        String note,
        SubOrderStatus status
) {}
