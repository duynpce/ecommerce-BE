package org.example.ticketservice.domain.constant;

/**
 * Mirrors the TransactionStatus enum in product-service.
 * These are the valid lifecycle states for a transaction
 * as driven by the buying-items-procedure Camunda process.
 */
public enum TransactionStatus {
    PENDING,
    PACKING,
    DELIVERING,
    NOT_RECEIVED,
    COMPLETED,
    REJECTED,
    RETURNED
}
