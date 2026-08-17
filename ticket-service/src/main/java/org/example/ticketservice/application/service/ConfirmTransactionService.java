package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public void confirm(UUID id, boolean approve) {
        String idStr = id.toString();

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

        taskService.complete(task.getId(), Map.of("approve", approve));

        log.info("[buying-items] Transaction/Sub-order {} by contributor: id={}",
                approve ? "approved" : "rejected", id);
    }
}
