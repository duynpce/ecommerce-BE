package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Service task: "product not received" (product-not-received).
 * Fires when the buyer reports the product was not received and retry < 3.
 * - Increments the retry counter for this snapshot iteration.
 * - Moves only this snapshot back to DELIVERING.
 * After this delegate the process loops back to the mock-delivery timer.
 */
@Slf4j
@Component("productNotReceivedDelegate")
@RequiredArgsConstructor
public class ProductNotReceivedDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    public void execute(DelegateExecution execution) {
        String subOrderIdStr = (String) execution.getVariable("subOrderId");
        String snapshotIdStr = (String) execution.getVariable("snapshotId");
        if (subOrderIdStr == null || snapshotIdStr == null) {
            throw new IllegalStateException(
                    "[buying-items] Missing subOrderId or snapshotId for not-received transition");
        }
        UUID subOrderId = UUID.fromString(subOrderIdStr);
        UUID snapshotId = UUID.fromString(snapshotIdStr);

        // Increment retry counter so the gateway can evaluate retry >= 3
        Integer retry = (Integer) execution.getVariable("retry");
        if (retry == null) retry = 0;
        execution.setVariable("retry", retry + 1);

        // This snapshot re-enters delivery while its sibling snapshots continue
        // independently in their own multi-instance executions.
        execution.setVariable("snapshot_status_" + snapshotIdStr, "DELIVERING");
        execution.setVariable("suborder_status_" + subOrderIdStr, "DELIVERING");
        productClient.updateSnapshotStatus(subOrderId, snapshotId, "DELIVERING");

        log.info("[buying-items] Snapshot not received; returning to DELIVERING: "
                        + "subOrderId={}, snapshotId={}, retry={}",
                subOrderId, snapshotId, retry + 1);
    }
}
