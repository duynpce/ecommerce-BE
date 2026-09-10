package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.example.ticketservice.application.client.ProductClient;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MarkSnapshotReviewAndCheckCompletionDelegateTest {

    @Test
    void reviewTimeoutCompletesSnapshotWithoutCallingProtectedReviewedEndpoint() {
        TestContext context = context(false);

        context.delegate().execute(context.execution());

        verify(context.productClient()).updateSnapshotStatus(
                context.subOrderId(), context.snapshotId(), "COMPLETED");
    }

    @Test
    void submittedReviewDoesNotRepeatProductServicesReviewedWrite() {
        TestContext context = context(true);

        context.delegate().execute(context.execution());

        verify(context.productClient()).updateSnapshotStatus(
                context.subOrderId(), context.snapshotId(), "COMPLETED");
    }

    private TestContext context(boolean reviewSubmitted) {
        UUID subOrderId = UUID.randomUUID();
        UUID snapshotId = UUID.randomUUID();
        Map<String, Object> variables = new HashMap<>();
        variables.put("subOrderId", subOrderId.toString());
        variables.put("currentSnapshotId", snapshotId.toString());
        variables.put("snapshot_reviewed_" + snapshotId, reviewSubmitted);
        variables.put("snapshot_status_" + snapshotId, "RECEIVED");
        variables.put(
                "snapshots_" + subOrderId,
                List.of(Map.of("snapshotId", snapshotId.toString())));

        DelegateExecution execution = mock(DelegateExecution.class);
        when(execution.getVariable(anyString()))
                .thenAnswer(invocation -> variables.get(invocation.getArgument(0, String.class)));
        doAnswer(invocation -> {
            variables.put(
                    invocation.getArgument(0, String.class),
                    invocation.getArgument(1));
            return null;
        }).when(execution).setVariable(anyString(), any());

        ProductClient productClient = mock(ProductClient.class);
        return new TestContext(
                new MarkSnapshotReviewAndCheckCompletionDelegate(productClient),
                execution,
                productClient,
                subOrderId,
                snapshotId);
    }

    private record TestContext(
            MarkSnapshotReviewAndCheckCompletionDelegate delegate,
            DelegateExecution execution,
            ProductClient productClient,
            UUID subOrderId,
            UUID snapshotId) {
    }
}
