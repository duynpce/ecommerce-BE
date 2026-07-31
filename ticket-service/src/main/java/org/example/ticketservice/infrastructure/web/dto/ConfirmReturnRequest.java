package org.example.ticketservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for POST /transaction-tickets/{transactionId}/confirm-return
 * Contributor confirms whether the returned product was received back.
 */
public record ConfirmReturnRequest(

        @NotNull(message = "received cannot be null")
        Boolean received
) {}
