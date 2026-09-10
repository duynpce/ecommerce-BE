package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.example.ticketservice.application.client.ProductClient;
import org.example.ticketservice.application.usecase.ConfirmShippedUseCase;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmShippedService implements ConfirmShippedUseCase {

    private final TaskService taskService;
    private final ProductClient productClient;

    @Override
    public void confirmShipped(UUID id) {
        String idStr = id.toString();

        // 1. Try task key confirm-delivery-to-transportation-agency or Activity_1yfk0z9 by subOrderId or transactionId
        Task task = taskService.createTaskQuery()
                .processVariableValueEquals("subOrderId", idStr)
                .taskDefinitionKey("confirm-delivery-to-transportation-agency")
                .singleResult();


        if (task == null) {
            throw new IllegalStateException(
                    "No pending shipping confirmation task found for id: " + id);
        }

        Map<String, Object> completionVariables = new HashMap<>();
        completionVariables.put("deliveredToAgency", true);
        completionVariables.put("suborder_status_" + idStr, "DELIVERING");

        Object snapshotsValue = taskService.getVariable(task.getId(), "snapshots_" + idStr);
        if (!(snapshotsValue instanceof List<?> snapshots) || snapshots.isEmpty()) {
            throw new IllegalStateException("No snapshots found for subOrderId: " + id);
        }
        for (Object value : snapshots) {
            if (!(value instanceof Map<?, ?> snapshot)
                    || !(snapshot.get("snapshotId") instanceof String snapshotId)) {
                throw new IllegalStateException("Invalid snapshot data for subOrderId: " + id);
            }
            completionVariables.put("snapshot_status_" + snapshotId, "DELIVERING");
        }

        // Synchronize product-service before advancing Camunda. The product
        // operation is idempotent, so retrying after a Camunda failure is safe.
        productClient.handoffSubOrder(id);
        taskService.complete(task.getId(), completionVariables);

        log.info("[buying-items] Sub-order handed to carrier; all snapshots -> DELIVERING: subOrderId={}", id);
    }
}
