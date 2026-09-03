package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.example.ticketservice.application.client.ProductClient;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReturnProductReceivedDelegateTest {

    @Test
    void returnsTheSnapshotThroughItsOwningSubOrder() {
        UUID subOrderId = UUID.randomUUID();
        UUID snapshotId = UUID.randomUUID();
        DelegateExecution execution = mock(DelegateExecution.class);
        ProductClient productClient = mock(ProductClient.class);
        when(execution.getVariable("subOrderId")).thenReturn(subOrderId.toString());
        when(execution.getVariable("snapshotId")).thenReturn(snapshotId.toString());

        new ReturnProductReceivedDelegate(productClient).execute(execution);

        verify(productClient).returnSnapshot(subOrderId, snapshotId);
    }
}
