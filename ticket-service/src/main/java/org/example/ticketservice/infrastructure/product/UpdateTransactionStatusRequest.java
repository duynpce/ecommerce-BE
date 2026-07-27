package org.example.ticketservice.infrastructure.product;

import org.example.ticketservice.domain.constant.TransactionStatus;

/**
 * Request body DTO sent to product-service PATCH /transactions/{id}/status.
 */
public record UpdateTransactionStatusRequest(TransactionStatus status) {}
