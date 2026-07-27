package org.example.productservice.infrastructure.web.dto.transaction;

import jakarta.validation.constraints.NotNull;
import org.example.productservice.domain.constant.TransactionStatus;

import java.util.UUID;

public record UpdateTransactionRequest(

        @NotNull(message = "Transaction ID is required")
        UUID id,

        TransactionStatus status
) {}
