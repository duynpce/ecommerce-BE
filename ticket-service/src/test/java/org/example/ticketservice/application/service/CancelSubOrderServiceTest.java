package org.example.ticketservice.application.service;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
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
class CancelSubOrderServiceTest {

    @Mock
    private RuntimeService runtimeService;
    @Mock
    private ExecutionQuery executionQuery;
    @Mock
    private Execution subscription;

    @Test
    void cancellationFindsTransactionScopedSubscriptionByProcessSubOrderIds() {
        UUID subOrderId = UUID.randomUUID();
        String subOrderIdValue = subOrderId.toString();
        String processInstanceId = "process-instance";
        String subscriptionId = "cancellation-subscription";

        when(runtimeService.createExecutionQuery()).thenReturn(executionQuery);
        when(executionQuery.messageEventSubscriptionName("user-cancel-msg"))
                .thenReturn(executionQuery);
        when(executionQuery.list()).thenReturn(List.of(subscription));
        when(subscription.getProcessInstanceId()).thenReturn(processInstanceId);
        when(subscription.getId()).thenReturn(subscriptionId);
        when(runtimeService.getVariable(processInstanceId, "subOrderIds"))
                .thenReturn(List.of(UUID.randomUUID().toString(), subOrderIdValue));
        when(runtimeService.getVariable(
                subscriptionId, "suborder_status_" + subOrderIdValue))
                .thenReturn("PACKING");

        new CancelSubOrderService(runtimeService)
                .cancel(subOrderId, "  Buyer changed their mind  ");

        InOrder calls = inOrder(runtimeService);
        calls.verify(runtimeService).setVariable(
                processInstanceId, "cancelledSubOrderId", subOrderIdValue);
        calls.verify(runtimeService).setVariable(
                processInstanceId, "cancelReason", "Buyer changed their mind");
        calls.verify(runtimeService).messageEventReceived(
                "user-cancel-msg",
                subscriptionId,
                Map.of("cancelReason", "Buyer changed their mind"));
    }
}
