package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Service task: "product not recieved" (Activity_180sl6b in returning-products process)
 * Fires when contributor reports that returned product was not received back.
 * Increments returnRetry process variable and loops back to mock return transit.
 */
@Slf4j
@Component("returnProductNotReceivedDelegate")
@RequiredArgsConstructor
public class ReturnProductNotReceivedDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        UUID transactionId = UUID.fromString((String) execution.getVariable("transactionId"));

        Integer returnRetry = (Integer) execution.getVariable("returnRetry");
        if (returnRetry == null) returnRetry = 0;
        execution.setVariable("returnRetry", returnRetry + 1);

        log.info("[returning-products] Returned product not received back (returnRetry={}): transactionId={}",
                returnRetry + 1, transactionId);
    }
}
