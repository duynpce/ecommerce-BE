package org.example.ticketservice.infrastructure.product.dto;

import org.example.ticketservice.domain.constant.TransactionStatus;

import java.util.UUID;

/**
 * Request body sent to product-service PATCH /transactions/{id}/complete.
 */
public record UpdateTransactionStatusRequest(
        TransactionStatus status,
        String reason,
        UUID triggerSubOrderId
) {}
