package org.example.ticketservice.application.client;

import org.example.ticketservice.domain.constant.TransactionStatus;

import java.util.UUID;

/**
 * Port for calling product-service to update transaction status
 * during the buying-items-procedure Camunda process.
 */
public interface ProductClient {

    void updateTransactionStatus(UUID transactionId, TransactionStatus status);

    void returnTransaction(UUID transactionId);
}

