package org.example.ticketservice.domain.constant;

/** Sub-order workflow states shared with product-service. */
public enum SubOrderStatus {
    WAITING_FOR_CONSOLIDATION,
    AWAITING_PICKUP,
    COMPLETED,
    RETURNED,
    PARTIALLY_RETURNED,
    CANCELLED,
    REJECTED
}
