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
 * Service task: "product received" (product-received)
 * Fires when the buyer confirms they received the product.
 * Updates transaction status → COMPLETED in product-service.
 */
@Slf4j
@Component("productReceivedDelegate")
@RequiredArgsConstructor
public class ProductReceivedDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    public void execute(DelegateExecution execution) {
        UUID transactionId = UUID.fromString((String) execution.getVariable("transactionId"));

        productClient.updateTransactionStatus(transactionId, TransactionStatus.COMPLETED);

        log.info("[buying-items] Product received — transaction COMPLETED: transactionId={}", transactionId);
    }
}
