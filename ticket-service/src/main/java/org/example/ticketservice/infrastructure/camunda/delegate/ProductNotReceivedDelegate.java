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
 * Service task: "product not received" (product-not-received)
 * Fires when the buyer reports the product was not received and retry < 3.
 * - Increments the "retry" process variable.
 * - Updates transaction status → NOT_RECEIVED in product-service.
 * After this delegate the process loops back to the mock-delivery timer.
 */
@Slf4j
@Component("productNotReceivedDelegate")
@RequiredArgsConstructor
public class ProductNotReceivedDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    public void execute(DelegateExecution execution) {
        UUID transactionId = UUID.fromString((String) execution.getVariable("transactionId"));

        // Increment retry counter so the gateway can evaluate retry >= 3
        Integer retry = (Integer) execution.getVariable("retry");
        if (retry == null) retry = 0;
        execution.setVariable("retry", retry + 1);

        log.info("[buying-items] Product not received (retry={}): transactionId={}", retry + 1, transactionId);
    }
}
