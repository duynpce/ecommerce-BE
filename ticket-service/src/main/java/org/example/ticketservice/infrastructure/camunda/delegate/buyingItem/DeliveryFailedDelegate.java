package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/** Marks an exhausted return-delivery attempt as failed for its parent process. */
@Slf4j
@Component("deliveryFailedDelegate")
public class DeliveryFailedDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        if (!Boolean.TRUE.equals(execution.getVariable("returnProcess"))) {
            throw new IllegalStateException(
                    "[delivery] DELIVERY_FAILED is only valid for a return delivery");
        }

        execution.setVariable("deliveryOutcome", "DELIVERY_FAILED");
        execution.setVariable("status", "DELIVERY_FAILED");
        log.warn("[delivery] Return delivery failed: snapshotId={}, retry={}",
                execution.getVariable("snapshotId"), execution.getVariable("retry"));
    }
}
