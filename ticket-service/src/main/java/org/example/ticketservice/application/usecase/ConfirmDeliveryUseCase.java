package org.example.ticketservice.application.usecase;

import java.util.UUID;

/**
 * Allows the buyer to confirm delivery status for a specific snapshot,
 * completing the "confirm-delivery-status" user task.
 * The status drives the exclusive gateway:
 *   RECEIVED     -> product-received -> COMPLETED
 *   NOT_RECEIVED -> retry loop (if retry < 3)
 *   RETURNED     -> product-returned call activity
 */
public interface ConfirmDeliveryUseCase {

    /**
     * @param subOrderId UUID of the snapshot's sub-order
     * @param snapshotId UUID of the snapshot multi-instance element
     * @param status     "RECEIVED", "NOT_RECEIVED", or "RETURNED"
     */
    void confirmDelivery(UUID subOrderId, UUID snapshotId, String status);
}
