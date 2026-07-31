package org.example.ticketservice.infrastructure.camunda.delegate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Service task: "product received" (ReturnSetStatus in returning-products process).
 * Fires when contributor confirms the returned product was received back.
 * Transitions transaction: DELIVERED → RETURNED in product-service.
 * Product-service also restores product stock on return.
 */
@Slf4j
@Component("returnProductReceivedDelegate")
@RequiredArgsConstructor
public class ReturnProductReceivedDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    public void execute(DelegateExecution execution) {
        UUID transactionId = UUID.fromString((String) execution.getVariable("transactionId"));

        productClient.returnTransaction(transactionId);

        log.info("[returning-products] Return completed and stock restored: transactionId={}", transactionId);
    }
}
