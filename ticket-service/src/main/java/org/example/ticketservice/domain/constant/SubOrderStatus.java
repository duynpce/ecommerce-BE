package org.example.ticketservice.domain.constant;

/** Terminal sub-order statuses calculated from snapshot process variables. */
public enum SubOrderStatus {
    COMPLETED,
    RETURNED,
    PARTIALLY_RETURNED,
    CANCELLED,
    REJECTED
}
