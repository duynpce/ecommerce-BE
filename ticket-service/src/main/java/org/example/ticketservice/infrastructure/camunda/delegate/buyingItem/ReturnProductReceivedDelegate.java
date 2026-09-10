package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Service task: "product received" (ReturnSetStatus in returning-products process).
 * Fires when contributor confirms the returned product was received back.
 * Marks the returned snapshot in its owning sub-order as RETURNED.
 * Product-service restores stock idempotently and recalculates the sub-order status.
 */
@Slf4j
@Component("returnProductReceivedDelegate")
@RequiredArgsConstructor
public class ReturnProductReceivedDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    public void execute(DelegateExecution execution) {
        UUID subOrderId = requiredUuid(execution, "subOrderId");
        UUID snapshotId = requiredUuid(execution, "snapshotId");

        productClient.returnSnapshot(subOrderId, snapshotId);

        log.info("[returning-products] Return completed and stock restored: "
                        + "subOrderId={}, snapshotId={}",
                subOrderId, snapshotId);
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
