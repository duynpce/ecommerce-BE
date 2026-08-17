package org.example.ticketservice.application.usecase;

import org.example.ticketservice.infrastructure.product.dto.SubOrderDto;

import java.util.List;
import java.util.UUID;

/**
 * Starts the buying-items-procedure Camunda process
 * for a transaction that has already been saved in product-service.
 */
public interface StartBuyingProcedureUseCase {

    /**
     * @param transactionId  UUID of the transaction (from product-service)
     * @param customerId     UUID of the buyer
     * @param subOrders      List of sub-orders with snapshots
     */
    void start(UUID transactionId, UUID customerId, List<SubOrderDto> subOrders);
}
