package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.example.ticketservice.application.usecase.ConfirmShippedUseCase;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmShippedService implements ConfirmShippedUseCase {

    private final TaskService taskService;

    @Override
    public void confirmShipped(UUID transactionId) {
        Task task = taskService.createTaskQuery()
                .processVariableValueEquals("transactionId", transactionId.toString())
                .taskDefinitionKey("delivered-to-transportation-confirmation")
                .singleResult();

        if (task == null) {
            throw new IllegalStateException(
                    "No pending shipping confirmation task found for transactionId: " + transactionId);
        }

        // No extra variables needed — gateway has already passed; just mark done
        taskService.complete(task.getId());

        log.info("[buying-items] Product shipped to transportation agency: transactionId={}", transactionId);
    }
}
