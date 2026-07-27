package org.example.ticketservice.application.usecase;

import java.util.UUID;

/**
 * Allows the buyer to confirm delivery status,
 * completing the "confirm-delivery-status" user task.
 * The status drives the exclusive gateway:
 *   RECEIVED     → product-received → COMPLETED
 *   NOT_RECEIVED → retry loop (if retry < 3)
 *   RETURNED     → product-returned call activity
 */
public interface ConfirmDeliveryUseCase {

    /**
     * @param transactionId UUID of the transaction (process variable key)
     * @param status        "RECEIVED", "NOT_RECEIVED", or "RETURNED"
     */
    void confirmDelivery(UUID transactionId, String status);
}
