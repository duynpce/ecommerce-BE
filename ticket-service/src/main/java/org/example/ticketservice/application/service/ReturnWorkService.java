package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.example.ticketservice.application.usecase.ReturnWorkUseCase;
import org.example.ticketservice.domain.exception.NotFoundException;
import org.example.ticketservice.domain.model.ReturnWorkItem;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReturnWorkService implements ReturnWorkUseCase {

    private static final String RETURN_TASK = "ReturnConfirm";
    private final TaskService taskService;

    @Override
    public List<ReturnWorkItem> getPendingReturns(UUID contributorId) {
        return taskService.createTaskQuery()
                .taskDefinitionKey(RETURN_TASK)
                .taskAssignee(contributorId.toString())
                .active()
                .orderByTaskCreateTime().asc()
                .list()
                .stream()
                .map(this::toWorkItem)
                .toList();
    }

    @Override
    public void confirmReturn(String taskId, UUID contributorId, boolean received) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskDefinitionKey(RETURN_TASK)
                .taskAssignee(contributorId.toString())
                .active()
                .singleResult();
        if (task == null) {
            throw new NotFoundException("Return task was not found or belongs to another contributor.");
        }
        taskService.complete(taskId, Map.of("status", received ? "RECEIVED" : "NOT_RECEIVED"));
    }

    private ReturnWorkItem toWorkItem(Task task) {
        String snapshotId = variable(task, "snapshotId");
        Object productName = taskService.getVariable(task.getId(), "productName");
        return new ReturnWorkItem(
                task.getId(),
                UUID.fromString(variable(task, "transactionId")),
                UUID.fromString(variable(task, "subOrderId")),
                UUID.fromString(snapshotId),
                optionalUuid(task, "shopId"),
                productName == null ? "Product " + snapshotId.substring(0, 8) : productName.toString(),
                retryVariable(task),
                task.getCreateTime() == null ? null : task.getCreateTime().toInstant());
    }

    private String variable(Task task, String name) {
        Object value = taskService.getVariable(task.getId(), name);
        if (value == null) throw new IllegalStateException("Return task is missing variable: " + name);
        return value.toString();
    }

    private UUID optionalUuid(Task task, String name) {
        Object value = taskService.getVariable(task.getId(), name);
        return value == null ? null : UUID.fromString(value.toString());
    }

    private int intVariable(Task task, String name) {
        Object value = taskService.getVariable(task.getId(), name);
        return value instanceof Number number ? number.intValue() : 0;
    }

    private int retryVariable(Task task) {
        Object retry = taskService.getVariable(task.getId(), "retry");
        return retry instanceof Number number
                ? number.intValue()
                : intVariable(task, "returnRetry");
    }
}
