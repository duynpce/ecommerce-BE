package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component("returnProductFailedDelegate")
@RequiredArgsConstructor
public class ReturnProductFailedDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        UUID transactionId = UUID.fromString((String) execution.getVariable("transactionId"));
        execution.setVariable("returnFailed", true);
        execution.setVariable("deliveryOutcome", "DELIVERY_FAILED");
        log.info("[returning-products] return product failed with transactionId: {}", transactionId);

    }
}
