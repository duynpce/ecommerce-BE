package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/** Completes a child delivery attempt and exposes its retry outcome to the caller. */
@Slf4j
@Component("deliveryRetryDelegate")
public class DeliveryRetryDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        int retry = numberVariable(execution, "retry");
        if (retry >= 3) {
            throw new IllegalStateException(
                    "[delivery] Retry outcome cannot be emitted after retry exhaustion");
        }
        execution.setVariable("deliveryOutcome", "RETRY");
        log.info("[delivery] Delivery attempt will be retried: snapshotId={}, retry={}",
                execution.getVariable("snapshotId"), retry);
    }

    private int numberVariable(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        if (!(value instanceof Number number)) {
            throw new IllegalStateException(
                    "[delivery] Missing " + variableName + " process variable");
        }
        return number.intValue();
    }
}
