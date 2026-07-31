package org.example.ticketservice.application.client;

import java.util.UUID;

/**
 * Port for calling product-service transaction state-transition endpoints
 * during the buying-items-procedure and returning-products Camunda processes.
 */
public interface ProductClient {

    /** Step 2a — PENDING → APPROVED */
    void approve(UUID transactionId);

    /** Step 2b — PENDING → REJECTED; product-service also restores stock */
    void reject(UUID transactionId);

    /** Step 3 — APPROVED → DELIVERED (fired after mock-delivery timer) */
    void deliver(UUID transactionId);

    /** Step 4 — DELIVERED → COMPLETED (buyer confirmed receipt) */
    void complete(UUID transactionId);

    /** Step 5 — DELIVERED → RETURNED; product-service also restores stock */
    void returnTransaction(UUID transactionId);
}
