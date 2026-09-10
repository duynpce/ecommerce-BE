package org.example.ticketservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import org.example.ticketservice.infrastructure.product.dto.SubOrderDto;

import java.util.List;
import java.util.UUID;

/**
 * Request body for POST /transaction-tickets/start
 * Caller must have already created the transaction in product-service
 * and provide the returned IDs and sub-orders here.
 */
public record StartBuyingProcedureRequest(

        @NotNull(message = "transactionId cannot be null")
        UUID transactionId,

        @NotNull(message = "customerId cannot be null")
        UUID customerId,

        List<SubOrderDto> subOrders
) {
    public StartBuyingProcedureRequest(UUID transactionId, UUID customerId) {
        this(transactionId, customerId, null);
    }
}
