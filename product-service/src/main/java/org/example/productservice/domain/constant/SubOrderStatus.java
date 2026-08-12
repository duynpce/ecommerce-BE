package org.example.productservice.domain.constant;

public enum SubOrderStatus {

    /**
     * Initial / active state: sub-order is created and at least one item
     * is still being processed (PENDING → PACKING → DELIVERING → RECEIVED).
     * This status persists until the sub-order reaches a terminal state.
     */
    PENDING,

    /**
     * Every item in this sub-order has been returned to the shop.
     * Terminal state.
     */
    RETURNED,

    /**
     * Some items were returned while others were completed.
     * Terminal state.
     */
    PARTIALLY_RETURNED,

    /**
     * The entire sub-order was cancelled before any item reached PACKING.
     * Terminal state.
     */
    CANCELLED,

    /**
     * All items in the sub-order have been successfully completed.
     * Terminal state.
     */
    COMPLETED,
}