package org.example.productservice.infrastructure.web.dto.transaction;

import jakarta.validation.constraints.NotNull;
import org.example.productservice.domain.constant.TransactionStatus;

/**
 * Request body used by ticket-service (via Camunda delegates) to update
 * a transaction's status during the buying-items-procedure lifecycle.
 */
public record UpdateTransactionStatusRequest(

        @NotNull(message = "Status cannot be null")
        TransactionStatus status
) {}
