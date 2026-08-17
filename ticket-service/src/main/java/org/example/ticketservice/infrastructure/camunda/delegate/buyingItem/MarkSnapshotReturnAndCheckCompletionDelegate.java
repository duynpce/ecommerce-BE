package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Marks the snapshot selected by the delivery step as returned, synchronizes
 * product-service, and records whether every snapshot in the sub-order has now
 * reached a terminal state.
 */
@Slf4j
@Component("markSnapshotReturnAndCheckCompletionDelegate")
@RequiredArgsConstructor
public class MarkSnapshotReturnAndCheckCompletionDelegate implements JavaDelegate {

    private static final Set<String> TERMINAL_STATUSES =
            Set.of("COMPLETED", "RETURNED", "CANCELLED", "REJECTED");

    private final ProductClient productClient;

    @Override
    @SuppressWarnings("unchecked")
    public void execute(DelegateExecution execution) {
        String subOrderIdValue = requireStringVariable(execution, "subOrderId");
        UUID subOrderId = parseUuid(subOrderIdValue, "subOrderId");

        Object snapshotsValue = execution.getVariable("snapshots_" + subOrderIdValue);
        if (!(snapshotsValue instanceof List<?> rawSnapshots) || rawSnapshots.isEmpty()) {
            throw new IllegalStateException(
                    "[buying-items] No snapshots found for subOrderId=" + subOrderIdValue);
        }
        List<Map<String, Object>> snapshots = (List<Map<String, Object>>) rawSnapshots;

        String currentSnapshotIdValue = (String) execution.getVariable("currentSnapshotId");
        String currentSnapshotId = currentSnapshotIdValue != null
                ? currentSnapshotIdValue
                : requireStringVariable(execution, "snapshotId");
        boolean belongsToSubOrder = snapshots.stream()
                .anyMatch(snapshot -> currentSnapshotId.equals(snapshot.get("snapshotId")));
        if (!belongsToSubOrder) {
            throw new IllegalStateException(
                    "[buying-items] Snapshot " + currentSnapshotId
                            + " does not belong to subOrderId=" + subOrderIdValue);
        }

        UUID snapshotId = parseUuid(currentSnapshotId, "currentSnapshotId");
        execution.setVariable("snapshot_status_" + currentSnapshotId, "RETURNED");
        execution.setVariable("snapshot_active_" + currentSnapshotId, false);

        // Let a failure propagate so Camunda can retry instead of advancing with
        // product-service and process state out of sync. Product-service treats
        // this update idempotently, so a retry does not restore stock twice.
        productClient.updateSnapshotStatus(subOrderId, snapshotId, "RETURNED");

        int terminalCount = 0;
        int returnedCount = 0;
        for (Map<String, Object> snapshot : snapshots) {
            String id = String.valueOf(snapshot.get("snapshotId"));
            String status = (String) execution.getVariable("snapshot_status_" + id);
            if (status != null && TERMINAL_STATUSES.contains(status)) {
                terminalCount++;
            }
            if ("RETURNED".equals(status)) {
                returnedCount++;
            }
        }

        boolean allSnapshotCompleted = terminalCount == snapshots.size();
        execution.setVariable("allSnapshotCompleted", allSnapshotCompleted);

        if (returnedCount == snapshots.size()) {
            execution.setVariable("suborder_status_" + subOrderIdValue, "RETURNED");
        } else if (allSnapshotCompleted) {
            execution.setVariable("suborder_status_" + subOrderIdValue, "PARTIALLY_RETURNED");
        }

        log.info("[buying-items] Snapshot returned: subOrderId={}, snapshotId={}, "
                        + "terminalSnapshots={}/{}, allSnapshotCompleted={}",
                subOrderId, snapshotId, terminalCount, snapshots.size(), allSnapshotCompleted);
    }

    private String requireStringVariable(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new IllegalStateException(
                    "[buying-items] Missing " + variableName + " process variable");
        }
        return text;
    }

    private UUID parseUuid(String value, String variableName) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "[buying-items] Invalid " + variableName + "=" + value, exception);
        }
    }
}
