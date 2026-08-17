package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Service task: "product received" (product-received).
 * Fires when the buyer confirms they received the product (delivery status = RECEIVED)
 * or when confirm status timeout (1m) occurs.
 * Transitions only the current snapshot from
 * DELIVERED_AWAITING_CONFIRMATION to RECEIVED.
 */
@Slf4j
@Component("productReceivedDelegate")
@RequiredArgsConstructor
public class ProductReceivedDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    public void execute(DelegateExecution execution) {
        String subOrderIdStr = (String) execution.getVariable("subOrderId");
        String snapshotIdStr = (String) execution.getVariable("snapshotId");
        if (subOrderIdStr == null || snapshotIdStr == null) {
            throw new IllegalStateException(
                    "[buying-items] Missing subOrderId or snapshotId for received transition");
        }

        UUID subOrderId = UUID.fromString(subOrderIdStr);
        UUID snapshotId = UUID.fromString(snapshotIdStr);

        String currentStatus = (String) execution.getVariable("snapshot_status_" + snapshotIdStr);
        if (!"DELIVERED_AWAITING_CONFIRMATION".equals(currentStatus)) {
            log.info("[buying-items] Snapshot received transition skipped: subOrderId={}, "
                            + "snapshotId={}, status={}",
                    subOrderId, snapshotId, currentStatus);
            return;
        }

        execution.setVariable("snapshot_status_" + snapshotIdStr, "RECEIVED");
        productClient.updateSnapshotStatus(subOrderId, snapshotId, "RECEIVED");

        log.info("[buying-items] Snapshot received and ready for review: subOrderId={}, snapshotId={}",
                subOrderId, snapshotId);
    }
}
