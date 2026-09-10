package org.example.ticketservice.application.service;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.example.ticketservice.application.client.ProductClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmDeliveryServiceTest {

    @Mock
    private TaskService taskService;
    @Mock
    private ProductClient productClient;
    @Mock
    private TaskQuery taskQuery;
    @Mock
    private Task task;

    @Test
    void staleDeliveringInstanceIsReconciledBeforeBuyerConfirmation() {
        UUID subOrderId = UUID.randomUUID();
        UUID snapshotId = UUID.randomUUID();
        String taskId = "buyer-confirmation";
        String statusVariable = "snapshot_status_" + snapshotId;

        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskDefinitionKey("confirm-delivery-status")).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(List.of(task));
        when(task.getId()).thenReturn(taskId);
        when(taskService.getVariable(taskId, "subOrderId")).thenReturn(subOrderId.toString());
        when(taskService.getVariable(taskId, "snapshotId")).thenReturn(snapshotId.toString());
        when(taskService.getVariable(taskId, statusVariable)).thenReturn("DELIVERING");

        new ConfirmDeliveryService(taskService, productClient)
                .confirmDelivery(subOrderId, snapshotId, "RECEIVED");

        verify(productClient).deliverSnapshot(subOrderId, snapshotId);
        verify(taskService).setVariable(
                taskId, statusVariable, "DELIVERED_AWAITING_CONFIRMATION");
        verify(taskService).complete(taskId, Map.of(
                "status", "RECEIVED",
                "currentSnapshotId", snapshotId.toString()));
    }
}
