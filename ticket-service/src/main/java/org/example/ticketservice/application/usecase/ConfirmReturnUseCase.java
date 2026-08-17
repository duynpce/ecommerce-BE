package org.example.ticketservice.application.usecase;

import java.util.UUID;

/**
 * Allows the contributor to confirm whether the returned product was received back
 * by completing the "ReturnConfirm" user task in the returning-products Camunda process.
 */
public interface ConfirmReturnUseCase {

    /**
     * @param snapshotId UUID of the product snapshot (process variable key)
     * @param received   true = received back, false = not received back
     */
    void confirmReturn(UUID snapshotId, boolean received);
}
