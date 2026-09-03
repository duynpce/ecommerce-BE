package org.example.ticketservice.application.service;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmTransactionServiceTest {

    @Mock
    private TaskService taskService;
    @Mock
    private RuntimeService runtimeService;
    @Mock
    private TaskQuery taskQuery;
    @Mock
    private Task task;

    @Test
    void rejectionStoresSubOrderIdAtProcessScopeBeforeCompletingTask() {
        UUID subOrderId = UUID.randomUUID();
        String taskId = "confirmation-task";
        String processInstanceId = "process-instance";

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskDefinitionKey("confirm-products-of-sub-order")).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(List.of(task));
        when(task.getId()).thenReturn(taskId);
        when(task.getProcessInstanceId()).thenReturn(processInstanceId);
        when(taskService.getVariable(taskId, "subOrderId"))
                .thenReturn(subOrderId.toString());

        new ConfirmTransactionService(taskService, runtimeService)
                .confirm(subOrderId, false, "Out of stock");

        InOrder calls = inOrder(runtimeService, taskService);
        calls.verify(runtimeService).setVariable(
                processInstanceId, "rejectedSubOrderId", subOrderId.toString());
        calls.verify(runtimeService).setVariable(
                processInstanceId, "rejectionReason", "Out of stock");
        calls.verify(taskService).complete(
                taskId, Map.of("approve", false, "rejectionReason", "Out of stock"));
    }
}
