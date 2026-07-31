package org.example.ticketservice.infrastructure.camunda.delegate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Service task: fires on the "rejected" branch after the contributor completes
 * the "confirm-the-transaction" user task with approve = false.
 * Transitions transaction: PENDING → REJECTED in product-service.
 * Product-service also restores reserved stock on rejection.
 */
@Slf4j
@Component("transactionRejectedDelegate")
@RequiredArgsConstructor
public class TransactionRejectedDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    public void execute(DelegateExecution execution) {
        UUID transactionId = UUID.fromString((String) execution.getVariable("transactionId"));

        productClient.reject(transactionId);

        log.info("[buying-items] Transaction rejected and stock restored: transactionId={}", transactionId);
    }
}
