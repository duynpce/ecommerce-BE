package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Service task: {@code create transaction and sub-order} ({@code create-transaction-and-sub-order}).
 *
 * <p>Fires immediately after the process starts (after {@link SyncDbStatusDelegate}).
 * It validates the variables prepared by {@code StartBuyingProcedureService}, then
 * creates the multi-instance collection consumed by the sub-process. Camunda creates
 * one sub-process execution for every value in {@code subOrderIds} and exposes that
 * value as {@code subOrderId}.
 * Responsibilities:
 * <ol>
 *   <li>Validate the transaction and prepared sub-order collection.</li>
 *   <li>Reject duplicate IDs, which would create duplicate sub-order executions.</li>
 *   <li>Validate the required variables for every sub-order.</li>
 * </ol>
 */
@Slf4j
@Component("createTransactionAndSubOrderDelegate")
public class CreateTransactionAndSubOderDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        Object transactionIdValue = execution.getVariable("transactionId");

        if (!(transactionIdValue instanceof String transactionIdString)) {
            throw new IllegalStateException("[buying-items] Missing transactionId process variable");
        }

        UUID transactionId = parseUuid(transactionIdString, "transactionId");

        Object subOrderIdsValue = execution.getVariable("subOrderIdsToCreate");
        if (!(subOrderIdsValue instanceof List<?> subOrderIdsToCreate) || subOrderIdsToCreate.isEmpty()) {
            throw new IllegalStateException(
                    "[buying-items] subOrderIdsToCreate must contain at least one sub-order for transactionId="
                            + transactionId);
        }
        requireListVariable(execution, "snapshotIds");
        requireListVariable(execution, "allSnapshots");

        if (!(execution.getVariable("subOrderSnapshotsMap") instanceof Map<?, ?> subOrderSnapshotsMap)) {
            throw new IllegalStateException("[buying-items] Missing subOrderSnapshotsMap");
        }

        Set<String> uniqueSubOrderIds = new HashSet<>();
        List<String> subOrderIds = new ArrayList<>(subOrderIdsToCreate.size());
        for (Object value : subOrderIdsToCreate) {
            if (!(value instanceof String subOrderId)) {
                throw new IllegalStateException(
                        "[buying-items] subOrderIdsToCreate must be a list of UUID strings");
            }

            parseUuid(subOrderId, "subOrderId");
            if (!uniqueSubOrderIds.add(subOrderId)) {
                throw new IllegalStateException("[buying-items] Duplicate subOrderId=" + subOrderId);
            }

            List<?> snapshots = requireListVariable(execution, "snapshots_" + subOrderId);
            List<?> snapshotIds = requireListVariable(execution, "snapshotIds_" + subOrderId);
            if (snapshots.size() != snapshotIds.size()) {
                throw new IllegalStateException(
                        "[buying-items] Snapshot data count mismatch for subOrderId=" + subOrderId);
            }

            if (!subOrderSnapshotsMap.containsKey(subOrderId)) {
                throw new IllegalStateException(
                        "[buying-items] subOrderSnapshotsMap is missing subOrderId=" + subOrderId);
            }

            if (execution.getVariable("suborder_status_" + subOrderId) == null) {
                throw new IllegalStateException("[buying-items] Missing status for subOrderId=" + subOrderId);
            }

            subOrderIds.add(subOrderId);
        }

        // The BPMN multi-instance sub-process consumes this collection immediately
        // after the delegate completes and creates one execution per sub-order ID.
        execution.setVariable("subOrderIds", subOrderIds);

        log.info("[buying-items] Created multi-instance sub-order collection: " +
                        "transactionId={}, subOrderCount={}",
                transactionId, subOrderIds.size());
    }

    private UUID parseUuid(String value, String variableName) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("[buying-items] Invalid " + variableName + "=" + value, exception);
        }
    }

    private List<?> requireListVariable(DelegateExecution execution, String variableName) {
        if (!(execution.getVariable(variableName) instanceof List<?> values)) {
            throw new IllegalStateException("[buying-items] Missing " + variableName);
        }
        return values;
    }
}
