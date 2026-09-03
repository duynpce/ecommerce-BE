package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.example.ticketservice.application.client.ProductClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReturnDeliveryDelegateTest {

    @Mock
    private DelegateExecution execution;
    @Mock
    private ProductClient productClient;

    @Test
    void successfulReturnDeliveryDoesNotApplyOutboundSnapshotTransition() {
        UUID subOrderId = UUID.randomUUID();
        UUID snapshotId = UUID.randomUUID();
        when(execution.getVariable("subOrderId")).thenReturn(subOrderId.toString());
        when(execution.getVariable("snapshotId")).thenReturn(snapshotId.toString());
        when(execution.getVariable("retry")).thenReturn(0);
        when(execution.getVariable("returnProcess")).thenReturn(true);

        new DeliverTheProductDelegate(productClient).execute(execution);

        verify(execution).setVariable("deliveryOutcome", "DELIVERED");
        verify(productClient, never()).deliverSnapshot(any(), any());
    }

    @Test
    void unsuccessfulReturnDeliveryOnlyAdvancesSharedRetryCounter() {
        UUID subOrderId = UUID.randomUUID();
        UUID snapshotId = UUID.randomUUID();
        when(execution.getVariable("subOrderId")).thenReturn(subOrderId.toString());
        when(execution.getVariable("snapshotId")).thenReturn(snapshotId.toString());
        when(execution.getVariable("retry")).thenReturn(1);
        when(execution.getVariable("returnProcess")).thenReturn(true);

        new ProductNotReceivedDelegate(productClient).execute(execution);

        verify(execution).setVariable("retry", 2);
        verify(execution).setVariable("returnRetry", 2);
        verify(productClient, never()).updateSnapshotStatus(any(), any(), any());
    }
}
