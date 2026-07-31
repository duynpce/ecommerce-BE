package org.example.ticketservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request body for POST /transaction-tickets/start
 * Caller must have already created the transaction in product-service
 * and provide the returned IDs here.
 */
public record StartBuyingProcedureRequest(

        @NotNull(message = "transactionId cannot be null")
        UUID transactionId,

        @NotNull(message = "contributorId cannot be null")
        UUID contributorId,

        @NotNull(message = "customerId cannot be null")
        UUID customerId
) {}
