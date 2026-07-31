package org.example.productservice.application.usecase;

import org.example.productservice.application.command.CreateTransactionCommand;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateTransactionCommand;
import org.example.productservice.application.criteria.TransactionSearchCriteria;
import org.example.productservice.domain.model.Transaction;

import java.util.UUID;

public interface TransactionUseCase {
    Transaction create(CreateTransactionCommand command);
    Transaction findById(UUID id);
    Transaction update(UpdateTransactionCommand command);
    void delete(UUID id);
    PageCommand<Transaction> search(TransactionSearchCriteria criteria);

    /**
     * Step 2a — Contributor approved the transaction.
     * Called by ticket-service Camunda delegate after "confirm-the-transaction" user task.
     */
    Transaction approve(UUID id);

    /**
     * Step 2b — Contributor rejected the transaction.
     * Called by ticket-service Camunda delegate after "confirm-the-transaction" user task.
     * Stock is restored on rejection.
     */
    Transaction reject(UUID id);

    /**
     * Step 3 — Contributor confirmed the product was handed to the transportation agency.
     * Called by ticket-service Camunda delegate after "delivered-to-transportation-confirmation" user task.
     */
    Transaction markShipped(UUID id);

    /**
     * Step 4 — Buyer confirmed the product was received successfully.
     * Called by ticket-service Camunda delegate after "confirm-delivery-status" user task resolves as RECEIVED.
     */
    Transaction complete(UUID id);

    /**
     * Step 5 — Return process completed.
     * Called by ticket-service Camunda delegate when the return procedure succeeds.
     * Sets status to RETURNED and restores product stock quantity.
     */
    Transaction returnTransaction(UUID id);
}
