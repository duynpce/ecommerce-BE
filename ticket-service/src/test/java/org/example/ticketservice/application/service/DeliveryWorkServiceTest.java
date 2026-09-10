package org.example.ticketservice.application.service;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.example.ticketservice.application.client.ProductClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryWorkServiceTest {

    private static final String TASK_ID = "delivery-task";

    @Mock
    private TaskService taskService;
    @Mock
    private ProductClient productClient;
    @Mock
    private TaskQuery taskQuery;
    @Mock
    private Task task;

    private DeliveryWorkService service;

    @BeforeEach
    void setUp() {
        service = new DeliveryWorkService(taskService, productClient);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId(TASK_ID)).thenReturn(taskQuery);
        when(taskQuery.taskDefinitionKey("Activity_1nc4s4r")).thenReturn(taskQuery);
        when(taskQuery.taskAssignee(org.mockito.ArgumentMatchers.anyString())).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(task);
    }

    @Test
    void receivedOutcomePersistsDeliveryBeforeCompletingCamundaTask() {
        UUID shipperId = UUID.randomUUID();
        UUID subOrderId = UUID.randomUUID();
        UUID snapshotId = UUID.randomUUID();
        when(task.getId()).thenReturn(TASK_ID);
        when(taskService.getVariable(TASK_ID, "returnProcess")).thenReturn(false);
        when(taskService.getVariable(TASK_ID, "subOrderId")).thenReturn(subOrderId.toString());
        when(taskService.getVariable(TASK_ID, "snapshotId")).thenReturn(snapshotId.toString());

        service.completeDelivery(TASK_ID, shipperId, "RECEIVED");

        verify(productClient).deliverSnapshot(subOrderId, snapshotId);
        ArgumentCaptor<Map<String, Object>> variables = ArgumentCaptor.forClass(Map.class);
        verify(taskService).complete(org.mockito.ArgumentMatchers.eq(TASK_ID), variables.capture());
        assertEquals("RECEIVED", variables.getValue().get("status"));
        assertEquals(
                "DELIVERED_AWAITING_CONFIRMATION",
                variables.getValue().get("snapshot_status_" + snapshotId));
        assertEquals(snapshotId.toString(), variables.getValue().get("currentSnapshotId"));
    }

    @Test
    void notReceivedOutcomeDoesNotMarkSnapshotDelivered() {
        service.completeDelivery(TASK_ID, UUID.randomUUID(), "NOT_RECEIVED");

        verify(productClient, never()).deliverSnapshot(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(taskService).complete(TASK_ID, Map.of("status", "NOT_RECEIVED"));
    }

    @Test
    void receivedReturnDoesNotApplyOutboundDeliveryTransition() {
        when(task.getId()).thenReturn(TASK_ID);
        when(taskService.getVariable(TASK_ID, "returnProcess")).thenReturn(true);

        service.completeDelivery(TASK_ID, UUID.randomUUID(), "RECEIVED");

        verify(productClient, never()).deliverSnapshot(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(taskService).complete(TASK_ID, Map.of("status", "RECEIVED"));
    }
}
