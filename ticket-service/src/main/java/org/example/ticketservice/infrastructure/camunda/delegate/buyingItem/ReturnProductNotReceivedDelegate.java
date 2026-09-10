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
 * Increments the shared retry counter before the BPMN process decides whether
 * another shipper pickup is allowed.
 */
@Slf4j
@Component("returnProductNotReceivedDelegate")
@RequiredArgsConstructor
public class ReturnProductNotReceivedDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        UUID transactionId = requiredUuid(execution, "transactionId");
        int retry = numberVariable(execution, "retry");
        int nextRetry = retry + 1;
        execution.setVariable("retry", nextRetry);
        // Keep the legacy variable synchronized for active instances created
        // by an older process definition.
        execution.setVariable("returnRetry", nextRetry);
        execution.setVariable("deliveryOutcome", "RETRY");

        log.info("[returning-products] Contributor did not receive return "
                        + "(retry={}): transactionId={}", nextRetry, transactionId);
    }

    private int numberVariable(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        return value instanceof Number number ? number.intValue() : 0;
    }

    private UUID requiredUuid(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new IllegalStateException(
                    "[returning-products] Missing " + variableName + " process variable");
        }
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "[returning-products] Invalid " + variableName + "=" + text,
                    exception);
        }
    }
}
