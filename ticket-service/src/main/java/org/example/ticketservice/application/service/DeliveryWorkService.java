package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.example.ticketservice.application.client.ProductClient;
import org.example.ticketservice.application.usecase.DeliveryWorkUseCase;
import org.example.ticketservice.domain.exception.NotFoundException;
import org.example.ticketservice.domain.model.DeliveryWorkItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryWorkService implements DeliveryWorkUseCase {

    private static final String PICKUP_TASK = "Activity_1uedxw3";
    private static final String RETURN_PICKUP_TASK = "Activity_0svpfqw";
    private static final String DELIVERY_TASK = "Activity_1nc4s4r";

    private final TaskService taskService;
    private final ProductClient productClient;

    @Override
    public List<DeliveryWorkItem> getWork(UUID shipperId) {
        List<DeliveryWorkItem> work = new ArrayList<>();
        addPickupWork(work, PICKUP_TASK, "AWAITING_PICKUP");
        addPickupWork(work, RETURN_PICKUP_TASK, "AWAITING_RETURN_PICKUP");
        taskService.createTaskQuery()
                .taskDefinitionKey(DELIVERY_TASK)
                .taskAssignee(shipperId.toString())
                .active()
                .orderByTaskCreateTime().asc()
                .list()
                .forEach(task -> work.add(toWorkItem(task,
                        booleanVariable(task, "returnProcess")
                                ? "RETURN_IN_DELIVERY"
                                : "IN_DELIVERY")));
        return work;
    }

    @Override
    public void acceptPickup(String taskId, UUID shipperId) {
        Task task = findTask(taskId, PICKUP_TASK);
        if (task == null) {
            task = findTask(taskId, RETURN_PICKUP_TASK);
        }
        if (task == null) {
            throw new NotFoundException("Delivery pickup task not found: " + taskId);
        }
        if (task.getAssignee() != null && !shipperId.toString().equals(task.getAssignee())) {
            throw new IllegalArgumentException("This pickup has already been accepted by another shipper.");
        }
        if (task.getAssignee() == null) {
            taskService.claim(taskId, shipperId.toString());
        }
        taskService.complete(taskId, Map.of("shipperId", shipperId.toString()));
    }

    @Override
    public void completeDelivery(String taskId, UUID shipperId, String outcome) {
        if (!"RECEIVED".equals(outcome) && !"NOT_RECEIVED".equals(outcome)) {
            throw new IllegalArgumentException("Delivery outcome must be RECEIVED or NOT_RECEIVED.");
        }
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskDefinitionKey(DELIVERY_TASK)
                .taskAssignee(shipperId.toString())
                .active()
                .singleResult();
        if (task == null) {
            throw new NotFoundException("Delivery task was not found or is assigned to another shipper.");
        }

        Map<String, Object> completionVariables = new HashMap<>();
        completionVariables.put("status", outcome);

        boolean returnProcess = booleanVariable(task, "returnProcess");
        if ("RECEIVED".equals(outcome) && !returnProcess) {
            UUID subOrderId = uuidVariable(task, "subOrderId");
            String snapshotIdValue = stringVariable(task, "snapshotId");
            UUID snapshotId = UUID.fromString(snapshotIdValue);

            // Persist the physical delivery before advancing Camunda. The
            // product-service transition is idempotent, so a retry is safe if
            // task completion fails after the HTTP call succeeds.
            productClient.deliverSnapshot(subOrderId, snapshotId);
            completionVariables.put(
                    "snapshot_status_" + snapshotIdValue,
                    "DELIVERED_AWAITING_CONFIRMATION");
            completionVariables.put("currentSnapshotId", snapshotIdValue);
        }

        taskService.complete(taskId, completionVariables);
    }

    private void addPickupWork(List<DeliveryWorkItem> work, String taskDefinitionKey, String stage) {
        taskService.createTaskQuery()
                .taskDefinitionKey(taskDefinitionKey)
                .taskCandidateGroup("SHIPPER")
                .active()
                .orderByTaskCreateTime().asc()
                .list()
                .forEach(task -> work.add(toWorkItem(task, stage)));
    }

    private Task findTask(String taskId, String definitionKey) {
        return taskService.createTaskQuery()
                .taskId(taskId)
                .taskDefinitionKey(definitionKey)
                .active()
                .singleResult();
    }

    private DeliveryWorkItem toWorkItem(Task task, String stage) {
        String snapshotId = stringVariable(task, "snapshotId");
        return new DeliveryWorkItem(
                task.getId(), stage,
                uuidVariable(task, "transactionId"),
                uuidVariable(task, "subOrderId"),
                UUID.fromString(snapshotId),
                productName(task, snapshotId),
                intVariable(task, "retry"),
                task.getCreateTime() == null ? null : task.getCreateTime().toInstant());
    }

    private String productName(Task task, String snapshotId) {
        Object directProductName = taskService.getVariable(task.getId(), "productName");
        if (directProductName != null) {
            return directProductName.toString();
        }
        Object snapshot = taskService.getVariable(task.getId(), "snapshot_" + snapshotId);
        if (snapshot instanceof Map<?, ?> values && values.get("name") != null) {
            return values.get("name").toString();
        }
        return "Product " + snapshotId.substring(0, 8);
    }

    private UUID uuidVariable(Task task, String name) {
        return UUID.fromString(stringVariable(task, name));
    }

    private String stringVariable(Task task, String name) {
        Object value = taskService.getVariable(task.getId(), name);
        if (value == null) throw new IllegalStateException("Delivery task is missing variable: " + name);
        return value.toString();
    }

    private int intVariable(Task task, String name) {
        Object value = taskService.getVariable(task.getId(), name);
        return value instanceof Number number ? number.intValue() : 0;
    }

    private boolean booleanVariable(Task task, String name) {
        return Boolean.TRUE.equals(taskService.getVariable(task.getId(), name));
    }
}
