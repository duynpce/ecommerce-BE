package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.RuntimeService;
import org.example.ticketservice.application.client.ProductClient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionRejectedDelegateTest {

    @Test
    void usesProcessScopedFallbackForOlderBpmnDefinitions() {
        UUID subOrderId = UUID.randomUUID();
        UUID snapshotId = UUID.randomUUID();
        DelegateExecution execution = mock(DelegateExecution.class);
        ProductClient productClient = mock(ProductClient.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        when(execution.getProcessInstanceId()).thenReturn("process-instance");
        when(execution.getVariable("subOrderId")).thenReturn(null);
        when(execution.getVariable("rejectedSubOrderId"))
                .thenReturn(subOrderId.toString());
        when(execution.getVariable("snapshots_" + subOrderId)).thenReturn(
                List.of(Map.of("snapshotId", snapshotId.toString())));

        new SubOrderRejectedDelegate(productClient, runtimeService).execute(execution);

        verify(execution).setVariable("suborder_status_" + subOrderId, "REJECTED");
        verify(execution).setVariable("snapshot_status_" + snapshotId, "REJECTED");
        verify(runtimeService).setVariable(
                "process-instance", "terminalSubOrderId", subOrderId.toString());
        verify(runtimeService).setVariable(
                "process-instance", "terminalReason", "Contributor confirmation timed out.");
        verify(productClient).rejectSubOrder(
                subOrderId, "Contributor confirmation timed out.");
    }

    @Test
    void missingSubOrderIdProducesActionableErrorInsteadOfNullPointerException() {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getProcessInstanceId()).thenReturn("process-instance");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> new SubOrderRejectedDelegate(
                        mock(ProductClient.class), mock(RuntimeService.class))
                        .execute(execution));

        assertTrue(error.getMessage().contains("Missing rejected sub-order ID"));
        assertTrue(error.getMessage().contains("process-instance"));
    }
}
