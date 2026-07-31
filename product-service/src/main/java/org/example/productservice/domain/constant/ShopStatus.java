package org.example.productservice.domain.constant;

public enum ShopStatus {

    /**
     * Shop has been created but is not yet open to customers.
     */
    INACTIVE,

    /**
     * Shop is active and visible to customers.
     */
    ACTIVE,

    /**
     * Shop is temporarily closed (e.g. maintenance, holiday).
     */
    SUSPENDED,

    /**
     * Shop has been permanently closed and is no longer accessible.
     */
    CLOSED
}
