package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.example.ticketservice.application.usecase.ConfirmTransactionUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmTransactionService implements ConfirmTransactionUseCase {

    private final TaskService taskService;
    private final RuntimeService runtimeService;

    @Override
    public void confirm(UUID id, boolean approve, String reason) {
        String idStr = id.toString();

        String normalizedReason = reason == null ? null : reason.trim();
        if (!approve && (normalizedReason == null || normalizedReason.isBlank())) {
            throw new IllegalArgumentException("A rejection reason is required");
        }

        // subOrderId is local to a multi-instance execution. A process-variable query
        // matches the whole transaction process and can therefore return every
        // confirmation task. Resolve it against each task's execution scope instead.
        List<Task> matchingTasks = taskService.createTaskQuery()
                .taskDefinitionKey("confirm-products-of-sub-order")
                .list()
                .stream()
                .filter(candidate -> idStr.equals(
                        taskService.getVariable(candidate.getId(), "subOrderId")))
                .toList();

        if (matchingTasks.isEmpty()) {
            throw new IllegalStateException(
                    "No pending confirmation task found for id: " + id);
        }
        if (matchingTasks.size() > 1) {
            throw new IllegalStateException(
                    "Multiple pending confirmation tasks found for sub-order id: " + id);
        }

        Task task = matchingTasks.getFirst();

        if (!approve) {
            // Older deployed process definitions invoke the rejection delegate
            // after leaving the multi-instance scope, where subOrderId is no
            // longer visible. Persist the ID at the root process scope so those
            // active instances can still be completed safely.
            runtimeService.setVariable(
                    task.getProcessInstanceId(), "rejectedSubOrderId", idStr);
            runtimeService.setVariable(
                    task.getProcessInstanceId(), "rejectionReason", normalizedReason);
        }

        Map<String, Object> completionVariables = new java.util.HashMap<>();
        completionVariables.put("approve", approve);
        if (!approve) {
            completionVariables.put("rejectionReason", normalizedReason);
        }
        taskService.complete(task.getId(), completionVariables);

        log.info("[buying-items] Transaction/Sub-order {} by contributor: id={}",
                approve ? "approved" : "rejected", id);
    }
}
