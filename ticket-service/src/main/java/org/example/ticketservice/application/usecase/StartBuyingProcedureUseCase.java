package org.example.ticketservice.application.usecase;

import java.util.UUID;

/**
 * Starts the buying-items-procedure Camunda process
 * for a transaction that has already been saved in product-service.
 */
public interface StartBuyingProcedureUseCase {

    /**
     * @param transactionId  UUID of the transaction (from product-service)
     * @param contributorId  UUID of the product contributor (seller)
     * @param customerId     UUID of the buyer
     */
    void start(UUID transactionId, UUID contributorId, UUID customerId);
}
