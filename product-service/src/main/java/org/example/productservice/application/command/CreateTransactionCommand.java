package org.example.productservice.application.command;

import org.example.productservice.infrastructure.web.dto.transaction.CreateTransactionItemRequest;

import java.util.List;
import java.util.UUID;

public record CreateTransactionCommand(
        UUID customerId,
        List<CreateTransactionItemRequest> items,
        List<String> voucherCodes,
        String phoneNumber,
        String address
) {
    public CreateTransactionCommand(UUID productId, UUID customerId, Integer quantity) {
        this(customerId, List.of(new CreateTransactionItemRequest(productId, quantity)), List.of(), null, null);
    }
}
