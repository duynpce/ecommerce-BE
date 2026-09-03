package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.example.ticketservice.application.client.ProductClient;
import org.example.ticketservice.domain.constant.TransactionStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionTerminalDelegatesTest {

    @Test
    void cancelTransactionPersistsCancelledStatus() {
        UUID transactionId = UUID.randomUUID();
        UUID triggerSubOrderId = UUID.randomUUID();
        UUID siblingSubOrderId = UUID.randomUUID();
        DelegateExecution execution = executionWithTransactionId(transactionId);
        ProductClient productClient = mock(ProductClient.class);
        when(execution.getVariable("terminalSubOrderId"))
                .thenReturn(triggerSubOrderId.toString());
        when(execution.getVariable("terminalReason")).thenReturn("Agency handoff timed out.");
        when(execution.getVariable("subOrderIds")).thenReturn(
                java.util.List.of(triggerSubOrderId.toString(), siblingSubOrderId.toString()));

        new TransactionCanceledDelegate(productClient).execute(execution);

        verify(execution).setVariable(
                "transaction_status_" + transactionId, TransactionStatus.CANCELLED.name());
        verify(execution).setVariable(
                "suborder_status_" + triggerSubOrderId, "CANCELLED");
        verify(execution).setVariable(
                "suborder_status_" + siblingSubOrderId, "CANCELLED");
        verify(productClient).complete(
                transactionId,
                TransactionStatus.CANCELLED,
                "Agency handoff timed out.",
                triggerSubOrderId);
    }

    @Test
    void rejectTransactionPersistsRejectedStatus() {
        UUID transactionId = UUID.randomUUID();
        UUID triggerSubOrderId = UUID.randomUUID();
        DelegateExecution execution = executionWithTransactionId(transactionId);
        ProductClient productClient = mock(ProductClient.class);
        when(execution.getVariable("terminalSubOrderId"))
                .thenReturn(triggerSubOrderId.toString());
        when(execution.getVariable("terminalReason"))
                .thenReturn("Rejected by contributor: Out of stock");

        new TransactionRejectedDelegate(productClient).execute(execution);

        verify(execution).setVariable(
                "transaction_status_" + transactionId, TransactionStatus.REJECTED.name());
        verify(productClient).complete(
                transactionId,
                TransactionStatus.REJECTED,
                "Rejected by contributor: Out of stock",
                triggerSubOrderId);
    }

    @Test
    void missingTransactionIdProducesActionableError() {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getProcessInstanceId()).thenReturn("process-instance");

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> new TransactionCanceledDelegate(mock(ProductClient.class)).execute(execution));

        assertTrue(error.getMessage().contains("Missing transactionId"));
        assertTrue(error.getMessage().contains("process-instance"));
    }

    private DelegateExecution executionWithTransactionId(UUID transactionId) {
        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable("transactionId")).thenReturn(transactionId.toString());
        return execution;
    }
}
