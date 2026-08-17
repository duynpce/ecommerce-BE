package org.example.productservice.domain.constant;

public enum SubOrderStatus {

    /**
     * Initial / active state: sub-order is created and at least one item
     * is still being processed.
     */
    PENDING,

    /**
     * Every item in this sub-order was rejected by the contributor.
     * Terminal state.
     */
    REJECTED,

    /**
     * Every item in this sub-order has been returned to the shop.
     * Terminal state.
     */
    RETURNED,

    /**
     * Some items were returned/cancelled while others were completed.
     * Terminal state.
     */
    PARTIALLY_RETURNED,

    /**
     * The sub-order was cancelled.
     * Terminal state.
     */
    CANCELLED,

    /**
     * All items in the sub-order have been successfully completed.
     * Terminal state.
     */
    COMPLETED,
}