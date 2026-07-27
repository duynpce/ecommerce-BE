package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.example.ticketservice.application.usecase.ConfirmReturnUseCase;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmReturnService implements ConfirmReturnUseCase {

    private final TaskService taskService;

    @Override
    public void confirmReturn(UUID transactionId, boolean received) {
        Task task = taskService.createTaskQuery()
                .processVariableValueEquals("transactionId", transactionId.toString())
                .taskDefinitionKey("ReturnConfirm")
                .singleResult();

        if (task == null) {
            throw new IllegalStateException(
                    "No pending return confirmation task found for transactionId: " + transactionId);
        }

        taskService.complete(task.getId(), Map.of("received", received));

        log.info("[returning-products] Contributor confirmed return received={}: transactionId={}",
                received, transactionId);
    }
}
