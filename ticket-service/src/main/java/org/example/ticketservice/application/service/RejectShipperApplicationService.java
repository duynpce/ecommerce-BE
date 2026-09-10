package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.example.ticketservice.application.command.RejectShipperApplicationCommand;
import org.example.ticketservice.application.usecase.RejectShipperApplicationUseCase;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RejectShipperApplicationService implements RejectShipperApplicationUseCase {
    private final TaskService taskService;

    @Override
    public void reject(RejectShipperApplicationCommand command) {
        Task task = taskService.createTaskQuery()
                .processVariableValueEquals("shipperApplicationTicketId", command.ticketId().toString())
                .taskDefinitionKey("review-shipper-application")
                .singleResult();
        if (task == null) {
            throw new IllegalStateException(
                    "No pending shipper application review: " + command.ticketId());
        }
        taskService.complete(task.getId(), Map.of("approved", false));
    }
}
