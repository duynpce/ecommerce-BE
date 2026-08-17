package org.example.productservice.domain.constant;

public enum TransactionStatus {
    /**
     * Initial state: transaction created, waiting for contributor confirmation.
     */
    PENDING,

    /**
     * All sub-orders in the transaction are completed.
     */
    COMPLETED,

    /**
     * All sub-orders in the transaction were rejected.
     */
    REJECTED,

    /**
     * All sub-orders in the transaction were cancelled.
     */
    CANCELLED,

    /**
     * All items of the transaction got returned.
     */
    RETURNED,

    /**
     * Some items of the transaction got returned or cancelled.
     */
    PARTIALLY_RETURNED,

    /**
     * Legacy: transaction failed due to system/payment error.
     */
    FAILED,
}
