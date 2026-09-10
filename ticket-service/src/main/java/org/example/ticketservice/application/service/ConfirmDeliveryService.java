package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.example.ticketservice.application.client.ProductClient;
import org.example.ticketservice.application.usecase.ConfirmDeliveryUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmDeliveryService implements ConfirmDeliveryUseCase {

    private final TaskService taskService;
    private final ProductClient productClient;

    @Override
    public void confirmDelivery(UUID subOrderId, UUID snapshotId, String status) {

        List<Task> matchingTasks = taskService.createTaskQuery()
                .taskDefinitionKey("confirm-delivery-status")
                .list()
                .stream()
                .filter(candidate -> subOrderId.toString().equals(
                        taskService.getVariable(candidate.getId(), "subOrderId")))
                .filter(candidate -> snapshotId.toString().equals(
                        taskService.getVariable(candidate.getId(), "snapshotId")))
                .toList();

        if (matchingTasks.isEmpty()) {
            throw new IllegalStateException(
                    "No pending delivery confirmation task found for subOrderId="
                            + subOrderId + ", snapshotId=" + snapshotId);
        }
        if (matchingTasks.size() > 1) {
            throw new IllegalStateException(
                    "Multiple pending delivery confirmation tasks found for subOrderId="
                            + subOrderId + ", snapshotId=" + snapshotId);
        }

        Task task = matchingTasks.getFirst();

        String snapshotStatusVariable = "snapshot_status_" + snapshotId;
        Object snapshotStatus = taskService.getVariable(task.getId(), snapshotStatusVariable);

        // A buyer confirmation task can only be reached after the shipper
        // reported RECEIVED. Reconcile process instances created before the
        // delivery transition was persisted by DeliveryWorkService.
        if ("DELIVERING".equals(snapshotStatus)) {
            productClient.deliverSnapshot(subOrderId, snapshotId);
            taskService.setVariable(
                    task.getId(), snapshotStatusVariable,
                    "DELIVERED_AWAITING_CONFIRMATION");
            snapshotStatus = "DELIVERED_AWAITING_CONFIRMATION";

            log.warn("[buying-items] Reconciled stale delivery status: subOrderId={}, "
                            + "snapshotId={}, oldStatus=DELIVERING, "
                            + "newStatus=DELIVERED_AWAITING_CONFIRMATION",
                    subOrderId, snapshotId);
        }

        if (!"DELIVERED_AWAITING_CONFIRMATION".equals(snapshotStatus)) {
            throw new IllegalStateException(
                    "Snapshot is not awaiting delivery confirmation: snapshotId="
                            + snapshotId + ", status=" + snapshotStatus);
        }

        // "status" drives the exclusive gateway:
        //   "RECEIVED"     → product-received delegate → COMPLETED
        //   "NOT_RECEIVED" → product-not-received delegate → retry loop (if retry < 3)
        //   "RETURNED"     → product-returned call activity
        taskService.complete(task.getId(), Map.of(
                "status", status,
                "currentSnapshotId", snapshotId.toString()));

        log.info("[buying-items] Snapshot delivery confirmed: subOrderId={}, snapshotId={}, status={}",
                subOrderId, snapshotId, status);
    }
}
