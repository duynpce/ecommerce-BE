package org.example.ticketservice.infrastructure.camunda.delegate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Service task: fires on the "approved" branch after the contributor completes
 * the "confirm-the-transaction" user task with approve = true.
 * Transitions transaction: PENDING → PACKING in product-service.
 */
@Slf4j
@Component("transactionApprovedDelegate")
@RequiredArgsConstructor
public class TransactionApprovedDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    public void execute(DelegateExecution execution) {
        UUID transactionId = UUID.fromString((String) execution.getVariable("transactionId"));

        productClient.approve(transactionId);

        log.info("[buying-items] Transaction approved: transactionId={}", transactionId);
    }
}
