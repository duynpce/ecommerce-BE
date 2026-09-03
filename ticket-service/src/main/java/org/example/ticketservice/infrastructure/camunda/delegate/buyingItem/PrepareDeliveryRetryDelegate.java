package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/** Resets the parent snapshot branch before assigning another delivery attempt. */
@Slf4j
@Component("prepareDeliveryRetryDelegate")
public class PrepareDeliveryRetryDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        Object snapshotId = execution.getVariable("snapshotId");
        Object retry = execution.getVariable("retry");
        if (!(snapshotId instanceof String) || !(retry instanceof Number)) {
            throw new IllegalStateException(
                    "[buying-items] Missing snapshotId or retry for delivery retry");
        }

        execution.setVariable("status", "DELIVERING");
        execution.setVariable("deliveryOutcome", "PENDING");
        log.info("[buying-items] Delivery retry prepared: snapshotId={}, retry={}",
                snapshotId, retry);
    }
}
