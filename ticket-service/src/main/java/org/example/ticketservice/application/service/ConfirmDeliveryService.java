package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
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

        Object snapshotStatus = taskService.getVariable(
                task.getId(), "snapshot_status_" + snapshotId);
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
