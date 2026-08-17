package org.example.ticketservice.infrastructure.product.dto;

import org.example.ticketservice.domain.constant.TransactionStatus;

/**
 * Request body sent to product-service PATCH /transactions/{id}/complete.
 */
public record UpdateTransactionStatusRequest(TransactionStatus status) {}
