package org.example.productservice.infrastructure.ticket.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartBuyingProcedureRequest(

        @NotNull(message = "transactionId cannot be null")
        UUID transactionId,

        @NotNull(message = "contributorId cannot be null")
        UUID contributorId,

        @NotNull(message = "customerId cannot be null")
        UUID customerId
) {}
