package org.example.ticketservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /transaction-tickets/{transactionId}/confirm
 * Contributor approves or rejects the transaction.
 */
public record ConfirmTransactionRequest(

        @NotNull(message = "approve cannot be null")
        Boolean approve,

        @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
        String reason
) {}
