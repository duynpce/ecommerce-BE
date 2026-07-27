package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.example.ticketservice.application.usecase.ConfirmDeliveryUseCase;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmDeliveryService implements ConfirmDeliveryUseCase {

    private final TaskService taskService;

    @Override
    public void confirmDelivery(UUID transactionId, String status) {
        Task task = taskService.createTaskQuery()
                .processVariableValueEquals("transactionId", transactionId.toString())
                .taskDefinitionKey("confirm-delivery-status")
                .singleResult();

        if (task == null) {
            throw new IllegalStateException(
                    "No pending delivery confirmation task found for transactionId: " + transactionId);
        }

        // "status" drives the exclusive gateway:
        //   "RECEIVED"     → product-received delegate → COMPLETED
        //   "NOT_RECEIVED" → product-not-received delegate → retry loop (if retry < 3)
        //   "RETURNED"     → product-returned call activity
        taskService.complete(task.getId(), Map.of("status", status));

        log.info("[buying-items] Delivery confirmed status='{}': transactionId={}", status, transactionId);
    }
}
