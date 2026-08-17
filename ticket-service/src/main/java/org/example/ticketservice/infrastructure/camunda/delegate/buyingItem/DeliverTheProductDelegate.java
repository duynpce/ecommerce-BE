package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Service task: {@code deliver the product} ({@code deliver-the-product})
 * inside the outer multi-instance sub-process {@code multi-instance-sub-process}.
 *
 * <p>Fires automatically after the {@code mock-delivery-process} timer (PT15S) expires.
 * Transitions only the current snapshot from DELIVERING to
 * DELIVERED_AWAITING_CONFIRMATION.
 *
 * <p>Operates at snapshot granularity via the nested multi-instance
 * {@code snapshotId} loop variable.
 */
@Slf4j
@Component("deliverTheProductDelegate")
@RequiredArgsConstructor
public class DeliverTheProductDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    public void execute(DelegateExecution execution) {
        String subOrderIdStr = (String) execution.getVariable("subOrderId");
        String snapshotIdStr = (String) execution.getVariable("snapshotId");
        if (subOrderIdStr == null || snapshotIdStr == null) {
            throw new IllegalStateException(
                    "[buying-items] Missing subOrderId or snapshotId in delivery execution");
        }

        UUID subOrderId = UUID.fromString(subOrderIdStr);
        UUID snapshotId = UUID.fromString(snapshotIdStr);

        execution.setVariableLocal("currentSnapshotId", snapshotIdStr);
        execution.setVariable("confirmExpireAt", Instant.now().plusSeconds(180).toString());
        execution.setVariable("reviewExpireAt", Instant.now().plusSeconds(300).toString());

        if (!(execution.getVariable("retry") instanceof Number)) {
            execution.setVariable("retry", 0);
        }

        String currentStatus = (String) execution.getVariable("snapshot_status_" + snapshotIdStr);
        if (!"DELIVERING".equals(currentStatus)) {
            log.info("[buying-items] Snapshot delivery transition skipped: subOrderId={}, "
                            + "snapshotId={}, status={}",
                    subOrderId, snapshotId, currentStatus);
            return;
        }

        productClient.deliverSnapshot(subOrderId, snapshotId);
        execution.setVariable(
                "snapshot_status_" + snapshotIdStr, "DELIVERED_AWAITING_CONFIRMATION");

        log.info("[buying-items] Snapshot delivered and awaiting buyer confirmation: "
                        + "subOrderId={}, snapshotId={}",
                subOrderId, snapshotId);
    }
}
