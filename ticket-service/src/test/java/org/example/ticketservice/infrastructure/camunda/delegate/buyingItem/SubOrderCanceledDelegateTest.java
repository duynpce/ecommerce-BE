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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubOrderCanceledDelegateTest {

    @Test
    void cancelsSubOrderWithoutOverwritingTerminalSnapshots() {
        UUID subOrderId = UUID.randomUUID();
        UUID activeSnapshotId = UUID.randomUUID();
        UUID completedSnapshotId = UUID.randomUUID();
        DelegateExecution execution = mock(DelegateExecution.class);
        ProductClient productClient = mock(ProductClient.class);
        RuntimeService runtimeService = mock(RuntimeService.class);

        when(execution.getVariable("subOrderId")).thenReturn(subOrderId.toString());
        when(execution.getVariable("snapshots_" + subOrderId)).thenReturn(List.of(
                Map.of("snapshotId", activeSnapshotId.toString()),
                Map.of("snapshotId", completedSnapshotId.toString())));
        when(execution.getVariable("snapshot_status_" + activeSnapshotId)).thenReturn("PACKING");
        when(execution.getVariable("snapshot_status_" + completedSnapshotId))
                .thenReturn("COMPLETED");
        when(execution.getVariable("cancelReason")).thenReturn("Buyer requested cancellation");
        when(execution.getProcessInstanceId()).thenReturn("process-instance");

        new SubOrderCanceledDelegate(productClient, runtimeService).execute(execution);

        verify(execution).setVariable("suborder_status_" + subOrderId, "CANCELLED");
        verify(execution).setVariable("snapshot_status_" + activeSnapshotId, "CANCELLED");
        verify(execution, never()).setVariable(
                "snapshot_status_" + completedSnapshotId, "CANCELLED");
        verify(productClient).cancelSubOrder(subOrderId, "Buyer requested cancellation");
        verify(runtimeService).setVariable(
                "process-instance", "terminalSubOrderId", subOrderId.toString());
        verify(runtimeService).setVariable(
                "process-instance", "terminalReason", "Buyer requested cancellation");
    }

    @Test
    void missingSubOrderIdProducesActionableError() {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getProcessInstanceId()).thenReturn("process-instance");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> new SubOrderCanceledDelegate(
                        mock(ProductClient.class), mock(RuntimeService.class)).execute(execution));

        assertTrue(error.getMessage().contains("Missing cancelled sub-order ID"));
        assertTrue(error.getMessage().contains("process-instance"));
    }
}
