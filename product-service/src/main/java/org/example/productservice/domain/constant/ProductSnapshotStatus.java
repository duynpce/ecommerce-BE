package org.example.productservice.domain.constant;

public enum ProductSnapshotStatus {

    /**
     * Initial state: item is part of a newly created sub-order,
     * waiting for the shop to start preparing it.
     */
    PENDING,

    /**
     * Shop rejected this item; it will not be delivered to the buyer.
     * Terminal failure state.
     */
    REJECTED,

    /**
     * Shop is preparing / packing this item.
     * Item has NOT yet been handed to a carrier.
     */
    PACKING,

    /**
     * Item has been handed to the carrier and is in transit to the buyer.
     */
    DELIVERING,

    /**
     * Carrier confirmed delivery; item is in the buyer's hands.
     * Awaiting buyer confirmation or auto-complete timer.
     */
    RECEIVED,

    /**
     * Buyer confirmed receipt and the review window has closed.
     * Terminal success state.
     */
    COMPLETED,

    /**
     * Item was returned to the shop.
     * Terminal failure / exception state.
     */
    RETURNED
}