package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.example.ticketservice.application.command.ApproveShipperApplicationCommand;
import org.example.ticketservice.application.usecase.ApproveShipperApplicationUseCase;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApproveShipperApplicationService implements ApproveShipperApplicationUseCase {
    private final TaskService taskService;

    @Override
    public void approve(ApproveShipperApplicationCommand command) {
        Task task = findReviewTask(command.ticketId().toString());
        taskService.complete(task.getId(), Map.of("approved", true));
    }

    private Task findReviewTask(String ticketId) {
        Task task = taskService.createTaskQuery()
                .processVariableValueEquals("shipperApplicationTicketId", ticketId)
                .taskDefinitionKey("review-shipper-application")
                .singleResult();
        if (task == null) {
            throw new IllegalStateException("No pending shipper application review: " + ticketId);
        }
        return task;
    }
}
