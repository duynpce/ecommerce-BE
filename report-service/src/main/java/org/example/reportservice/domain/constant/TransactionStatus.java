package org.example.reportservice.domain.constant;

public enum TransactionStatus {
    /**
     * Initial state: transaction created, waiting for contributor confirmation.
     */
    PENDING,

    /**
     * Contributor approved the transaction; product is being packed.
     */
    PACKING,

    /**
     * Product handed to transportation agency; in transit to buyer.
     */
    DELIVERING,

    /**
     * Buyer reported product not received; pending re-delivery.
     */
    NOT_RECEIVED,

    /**
     * Transaction completed successfully; product received by buyer.
     */
    COMPLETED,

    /**
     * Contributor rejected the transaction.
     */
    REJECTED,

    /**
     * Buyer returned the product; returning procedure activated.
     */
    RETURNED,

    /**
     * Legacy: transaction failed due to system/payment error.
     */
    FAILED,

    /**
     * Legacy: transaction was reversed.
     */
    REVERSED
}
