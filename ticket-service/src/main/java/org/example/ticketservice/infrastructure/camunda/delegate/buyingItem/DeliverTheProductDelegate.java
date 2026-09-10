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
 * Service task: {@code item received} in {@code delivery-process}.
 *
 * <p>Fires after the shipper completes the real delivery confirmation task.
 * Transitions the current snapshot from DELIVERING to
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
        if (!(execution.getVariable("retry") instanceof Number)) {
            execution.setVariable("retry", 0);
        }

        if (Boolean.TRUE.equals(execution.getVariable("returnProcess"))) {
            // The normal delivery transition would move the snapshot back to
            // DELIVERED_AWAITING_CONFIRMATION. A return delivery must keep the
            // snapshot in its return state until the contributor confirms it.
            execution.setVariable("deliveryOutcome", "DELIVERED");
            log.info("[delivery] Return delivered to contributor: subOrderId={}, snapshotId={}",
                    subOrderId, snapshotId);
            return;
        }

        execution.setVariable("confirmExpireAt", Instant.now().plusSeconds(180).toString());
        execution.setVariable("reviewExpireAt", Instant.now().plusSeconds(300).toString());

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
        execution.setVariable("deliveryOutcome", "DELIVERED");

        log.info("[buying-items] Snapshot delivered and awaiting buyer confirmation: "
                        + "subOrderId={}, snapshotId={}",
                subOrderId, snapshotId);
    }
}
