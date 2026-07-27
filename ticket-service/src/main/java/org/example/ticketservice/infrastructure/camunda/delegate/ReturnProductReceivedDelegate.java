package org.example.ticketservice.infrastructure.camunda.delegate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Service task: "product received" (ReturnSetStatus in returning-products process)
 * Fires when contributor confirms the returned product was received.
 * Calls product-service to mark status RETURNED and add the product's quantity back to stock.
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

        log.info("[returning-products] Return process completed and stock restored for transactionId={}", transactionId);
    }
}
