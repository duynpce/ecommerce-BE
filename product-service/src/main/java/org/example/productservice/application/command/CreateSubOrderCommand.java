package org.example.productservice.application.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateSubOrderCommand(
        UUID transactionId,
        UUID shopId,
        UUID customerId,
        BigDecimal shippingFee,
        String note,
        List<Item> items
) {
    public record Item(
            UUID productId,
            Integer quantity
    ) {}
}
