package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Records one transport-agency confirmation and decides whether every
 * sub-order in the transaction has now been confirmed.
 */
@Slf4j
@Component("completeConfirmationSubProcessAndCheckCompletionDelegate")
public class CompleteConfirmationSubProcessAndCheckCompletionDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        String currentSubOrderId = requireString(execution, "subOrderId");
        List<String> subOrderIds = requireSubOrderIds(execution);
        if (!subOrderIds.contains(currentSubOrderId)) {
            throw new IllegalStateException(
                    "[buying-items] Current sub-order is not part of the transaction: "
                            + currentSubOrderId);
        }

        execution.setVariable("suborder_confirmed_" + currentSubOrderId, true);

        boolean allSubOrderConfirmed = subOrderIds.stream().allMatch(subOrderId ->
                subOrderId.equals(currentSubOrderId)
                        || Boolean.TRUE.equals(execution.getVariable(
                                "suborder_confirmed_" + subOrderId)));
        execution.setVariable("allSubOrderConfirmed", allSubOrderConfirmed);

        log.info("[buying-items] Transport-agency confirmation recorded: "
                        + "subOrderId={}, confirmedAll={}",
                currentSubOrderId, allSubOrderConfirmed);
    }

    private String requireString(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new IllegalStateException(
                    "[buying-items] Missing " + variableName + " process variable");
        }
        return text;
    }

    @SuppressWarnings("unchecked")
    private List<String> requireSubOrderIds(DelegateExecution execution) {
        Object value = execution.getVariable("subOrderIds");
        if (!(value instanceof List<?> ids) || ids.isEmpty()
                || ids.stream().anyMatch(id -> !(id instanceof String))) {
            throw new IllegalStateException(
                    "[buying-items] Missing or invalid subOrderIds process variable");
        }
        return (List<String>) ids;
    }
}
