package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Service task: {@code Mark Snapshot Reviewed &amp; Check Completion}
 * ({@code mark-snapshot-reviewed-check-completiona}) inside the
 * outer multi-instance sub-process.
 *
 * <p>BPMN Documentation:
 * "set is review to true and complete sub-order if all of snapshot is review is true"
 *
 * <p>Fires after:
 * <ul>
 *   <li>The buyer completes the "review product" user task ({@code review-product}), OR</li>
 *   <li>The {@code review-time-out} boundary timer fires (auto-complete on deadline).</li>
 * </ul>
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Mark the current snapshot (identified by {@code currentSnapshotId}) as reviewed in
 *       Camunda process variables ({@code snapshot_reviewed_&lt;snapshotId&gt; = true}).</li>
 *   <li>Sync the reviewed status back to product-service for the current snapshot.</li>
 *   <li>Complete only the current snapshot.</li>
 *   <li>Check whether every snapshot of this sub-order is terminal.</li>
 *   <li>Set {@code allSnapshotCompleted} for the completion gateway.</li>
 * </ol>
 */
@Slf4j
@Component("markSnapshotReviewAndCheckCompletionDelegate")
@RequiredArgsConstructor
public class MarkSnapshotReviewAndCheckCompletionDelegate implements JavaDelegate {

    private static final Set<String> TERMINAL_STATUSES =
            Set.of("COMPLETED", "RETURNED", "CANCELLED", "REJECTED");

    private final ProductClient productClient;

    @Override
    @SuppressWarnings("unchecked")
    public void execute(DelegateExecution execution) {
        String subOrderIdStr    = (String) execution.getVariable("subOrderId");
        String currentSnapshotId = (String) execution.getVariable("currentSnapshotId");
        if (currentSnapshotId == null) {
            currentSnapshotId = (String) execution.getVariable("snapshotId");
        }

        if (subOrderIdStr == null || currentSnapshotId == null) {
            throw new IllegalStateException(
                    "[buying-items] Missing subOrderId or snapshotId for review completion");
        }

        // 1. Mark only the current snapshot as reviewed and completed.
        execution.setVariable("snapshot_reviewed_" + currentSnapshotId, true);
        execution.setVariable("snapshot_status_" + currentSnapshotId, "COMPLETED");

        // 2. Keep product-service synchronized. Failures propagate so Camunda
        // can retry rather than finishing this snapshot with divergent state.
        UUID subOrderId = UUID.fromString(subOrderIdStr);
        UUID snapshotId = UUID.fromString(currentSnapshotId);
        productClient.markSnapshotIsReviewed(subOrderId, snapshotId, true);
        productClient.updateSnapshotStatus(subOrderId, snapshotId, "COMPLETED");
        log.info("[buying-items] Snapshot review completed: subOrderId={}, snapshotId={}",
                subOrderId, snapshotId);

        // 3. Check whether every snapshot in this sub-order has reached a
        // terminal state. Returned/cancelled/rejected snapshots do not require
        // a review, but they still count toward sub-order completion.
        List<Map<String, Object>> snapshots =
                (List<Map<String, Object>>) execution.getVariable("snapshots_" + subOrderIdStr);

        boolean allCompleted = true;
        int totalSnapshots  = 0;
        int completedCount  = 0;

        if (snapshots != null && !snapshots.isEmpty()) {
            totalSnapshots = snapshots.size();
            for (Map<String, Object> snapshot : snapshots) {
                String candidateSnapshotId = (String) snapshot.get("snapshotId");
                String status = (String) execution.getVariable(
                        "snapshot_status_" + candidateSnapshotId);
                if (status != null && TERMINAL_STATUSES.contains(status)) {
                    completedCount++;
                } else {
                    allCompleted = false;
                }
            }
        } else {
            throw new IllegalStateException(
                    "[buying-items] No snapshots found for subOrderId=" + subOrderIdStr);
        }

        // 4. Drive the exclusive gateway
        execution.setVariable("allSnapshotCompleted", allCompleted);

        log.info("[buying-items] Snapshot completion check: subOrderId={}, terminal={}/{}, "
                        + "allSnapshotCompleted={}",
                subOrderIdStr, completedCount, totalSnapshots, allCompleted);
    }
}
