package org.example.ticketservice.infrastructure.product.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SubOrderDto(
        UUID id,
        UUID transactionId,
        UUID shopId,
        UUID customerId,
        List<ProductSnapshotDto> items,
        BigDecimal subTotalAmount,
        BigDecimal shippingFee,
        BigDecimal totalAmount,
        String note,
        String status
) {}
