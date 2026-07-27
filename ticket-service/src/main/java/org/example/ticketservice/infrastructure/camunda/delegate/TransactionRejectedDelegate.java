package org.example.ticketservice.infrastructure.camunda.delegate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.example.ticketservice.domain.constant.TransactionStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Service task: "transaction rejected" (transaction-rejected)
 * Fires when contributor rejects the transaction.
 * Updates transaction status → REJECTED in product-service.
 */
@Slf4j
@Component("transactionRejectedDelegate")
@RequiredArgsConstructor
public class TransactionRejectedDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    public void execute(DelegateExecution execution) {
        UUID transactionId = UUID.fromString((String) execution.getVariable("transactionId"));

        productClient.updateTransactionStatus(transactionId, TransactionStatus.REJECTED);

        log.info("[buying-items] Transaction rejected: transactionId={}", transactionId);
    }
}
