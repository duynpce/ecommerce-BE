package org.example.ticketservice.application.usecase;

import java.util.UUID;

/**
 * Allows the contributor to confirm the product was handed over
 * to the transportation agency, completing the
 * "delivered-to-transportation-confirmation" user task.
 */
public interface ConfirmShippedUseCase {

    /**
     * @param transactionId UUID of the transaction (process variable key)
     */
    void confirmShipped(UUID transactionId);
}
