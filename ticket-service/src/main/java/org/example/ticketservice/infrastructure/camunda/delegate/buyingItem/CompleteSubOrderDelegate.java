package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.example.ticketservice.domain.constant.SubOrderStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Completes one sub-order after every snapshot has reached a terminal status.
 * The resulting sub-order status is sent to product-service without modifying
 * any snapshot status.
 */
@Slf4j
@Component("completeSubOrderDelegate")
@RequiredArgsConstructor
public class CompleteSubOrderDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    @SuppressWarnings("unchecked")
    public void execute(DelegateExecution execution) {
        String subOrderIdValue = (String) execution.getVariable("subOrderId");
        if (subOrderIdValue == null) {
            throw new IllegalStateException("Cannot complete sub-order: subOrderId is missing");
        }

        List<Map<String, Object>> snapshots =
                (List<Map<String, Object>>) execution.getVariable("snapshots_" + subOrderIdValue);
        if (snapshots == null || snapshots.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot complete sub-order " + subOrderIdValue + ": snapshots are missing");
        }

        int completedCount = 0;
        int returnedCount = 0;
        int cancelledCount = 0;
        int rejectedCount = 0;

        for (Map<String, Object> snapshot : snapshots) {
            String snapshotId = (String) snapshot.get("snapshotId");
            if (snapshotId == null) {
                throw new IllegalStateException(
                        "Cannot complete sub-order " + subOrderIdValue + ": snapshotId is missing");
            }

            String currentStatus =
                    (String) execution.getVariable("snapshot_status_" + snapshotId);
            switch (currentStatus == null ? "" : currentStatus) {
                case "COMPLETED" -> completedCount++;
                case "RETURNED" -> returnedCount++;
                case "CANCELLED" -> cancelledCount++;
                case "REJECTED" -> rejectedCount++;
                default -> throw new IllegalStateException(
                        "Cannot complete sub-order " + subOrderIdValue + ": snapshot "
                                + snapshotId + " is not terminal (status=" + currentStatus + ")");
            }
        }

        SubOrderStatus status = determineStatus(
                completedCount,
                returnedCount,
                cancelledCount,
                rejectedCount,
                snapshots.size());

        execution.setVariable("suborder_status_" + subOrderIdValue, status.name());

        UUID subOrderId = UUID.fromString(subOrderIdValue);
        productClient.completeSubOrder(subOrderId, status);

        log.info("[buying-items] Completed sub-order and synced status: subOrderId={}, status={}",
                subOrderId, status);
    }

    private SubOrderStatus determineStatus(
            int completedCount,
            int returnedCount,
            int cancelledCount,
            int rejectedCount,
            int total) {
        if (completedCount == total) {
            return SubOrderStatus.COMPLETED;
        }
        if (returnedCount == total) {
            return SubOrderStatus.RETURNED;
        }
        if (cancelledCount == total) {
            return SubOrderStatus.CANCELLED;
        }
        if (rejectedCount == total) {
            return SubOrderStatus.REJECTED;
        }
        return SubOrderStatus.PARTIALLY_RETURNED;
    }
}
