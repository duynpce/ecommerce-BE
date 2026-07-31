package org.example.ticketservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for POST /transaction-tickets/{transactionId}/confirm
 * Contributor approves or rejects the transaction.
 */
public record ConfirmTransactionRequest(

        @NotNull(message = "approve cannot be null")
        Boolean approve
) {}
