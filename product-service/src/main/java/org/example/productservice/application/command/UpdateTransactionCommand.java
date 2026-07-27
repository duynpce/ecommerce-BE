package org.example.productservice.application.command;

import org.example.productservice.domain.constant.TransactionStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateTransactionCommand(
        UUID id,
        TransactionStatus status
) {}
