package org.example.productservice.infrastructure.ticket.dto;

import jakarta.validation.constraints.NotNull;
import org.example.productservice.infrastructure.web.dto.suborder.SubOrderResponse;

import java.util.List;
import java.util.UUID;

public record StartBuyingProcedureRequest(

        @NotNull(message = "transactionId cannot be null")
        UUID transactionId,

        @NotNull(message = "customerId cannot be null")
        UUID customerId,

        List<SubOrderResponse> subOrders
) {
    public StartBuyingProcedureRequest(UUID transactionId, UUID customerId) {
        this(transactionId, customerId, null);
    }
}
