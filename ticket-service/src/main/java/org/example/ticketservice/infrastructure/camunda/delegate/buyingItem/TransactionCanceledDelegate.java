package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.example.ticketservice.domain.constant.TransactionStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.List;
import java.util.Map;

/** Ends the parent transaction with the CANCELLED status. */
@Slf4j
@Component("TransactionCanceledDelegate")
@RequiredArgsConstructor
public class TransactionCanceledDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    public void execute(DelegateExecution execution) {
        String transactionIdValue = requiredTransactionId(execution);
        UUID transactionId = UUID.fromString(transactionIdValue);
        String triggerSubOrderValue = firstNonBlank(
                (String) execution.getVariable("terminalSubOrderId"),
                (String) execution.getVariable("cancelledSubOrderId"));
        UUID triggerSubOrderId = triggerSubOrderValue == null
                ? null
                : UUID.fromString(triggerSubOrderValue);
        String sourceReason = firstNonBlank(
                (String) execution.getVariable("terminalReason"),
                firstNonBlank(
                        (String) execution.getVariable("cancelReason"),
                        (String) execution.getVariable("cancelErrorMessage")));
        if (sourceReason == null) {
            sourceReason = "The transaction was cancelled by the workflow.";
        }
        execution.setVariable(
                "transaction_status_" + transactionIdValue,
                TransactionStatus.CANCELLED.name());
        markAllSubOrdersCancelled(execution);
        productClient.complete(
                transactionId, TransactionStatus.CANCELLED, sourceReason, triggerSubOrderId);

        log.info("[buying-items] Transaction cancelled: transactionId={}", transactionId);
    }

    private String requiredTransactionId(DelegateExecution execution) {
        Object value = execution.getVariable("transactionId");
        if (!(value instanceof String transactionId) || transactionId.isBlank()) {
            throw new IllegalStateException(
                    "[buying-items] Missing transactionId while cancelling process instance "
                            + execution.getProcessInstanceId());
        }
        return transactionId;
    }

    @SuppressWarnings("unchecked")
    private void markAllSubOrdersCancelled(DelegateExecution execution) {
        Object ids = execution.getVariable("subOrderIds");
        if (!(ids instanceof List<?> subOrderIds)) return;
        for (Object id : subOrderIds) {
            if (id instanceof String subOrderId) {
                execution.setVariable("suborder_status_" + subOrderId, "CANCELLED");
                markSnapshotsCancelled(execution, subOrderId);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void markSnapshotsCancelled(DelegateExecution execution, String subOrderId) {
        Object value = execution.getVariable("snapshots_" + subOrderId);
        if (!(value instanceof List<?> snapshots)) return;
        for (Object entry : snapshots) {
            if (entry instanceof Map<?, ?> snapshot
                    && snapshot.get("snapshotId") instanceof String snapshotId) {
                execution.setVariable("snapshot_status_" + snapshotId, "CANCELLED");
            }
        }
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank()
                ? first.trim()
                : second == null || second.isBlank() ? null : second.trim();
    }
}
