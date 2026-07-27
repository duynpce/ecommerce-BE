package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.example.ticketservice.application.usecase.ConfirmTransactionUseCase;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmTransactionService implements ConfirmTransactionUseCase {

    private final TaskService taskService;

    @Override
    public void confirm(UUID transactionId, boolean approve) {
        Task task = taskService.createTaskQuery()
                .processVariableValueEquals("transactionId", transactionId.toString())
                .taskDefinitionKey("confirm-the-transaction")
                .singleResult();

        if (task == null) {
            throw new IllegalStateException(
                    "No pending confirmation task found for transactionId: " + transactionId);
        }

        taskService.complete(task.getId(), Map.of("approve", approve));

        log.info("[buying-items] Transaction {} by contributor: transactionId={}",
                approve ? "approved" : "rejected", transactionId);
    }
}
