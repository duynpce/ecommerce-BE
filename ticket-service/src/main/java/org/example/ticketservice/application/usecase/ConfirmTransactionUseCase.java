package org.example.ticketservice.application.usecase;

import java.util.UUID;

/**
 * Allows the contributor to confirm or reject the transaction
 * by completing the "confirm-the-transaction" user task.
 */
public interface ConfirmTransactionUseCase {

    /**
     * @param transactionId UUID of the transaction (used as process variable key)
     * @param approve       true = approve, false = reject
     */
    void confirm(UUID transactionId, boolean approve);
}
