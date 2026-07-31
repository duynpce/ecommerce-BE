package org.example.ticketservice.infrastructure.camunda.delegate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Service task: "deliver the product" (deliver-the-product).
 * Fires automatically after the 30-second mock-delivery timer expires.
 * Transitions transaction: APPROVED → DELIVERED in product-service.
 */
@Slf4j
@Component("deliverTheProductDelegate")
@RequiredArgsConstructor
public class DeliverTheProductDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    public void execute(DelegateExecution execution) {
        UUID transactionId = UUID.fromString((String) execution.getVariable("transactionId"));

        productClient.deliver(transactionId);

        log.info("[buying-items] Product in transit (DELIVERED): transactionId={}", transactionId);
    }
}
