package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Service task: {@code cancel order} ({@code order-cancel}).
 *
 * <p>Handles two cancellation triggers that share this delegate:
 * <ol>
 *   <li><strong>User cancellation</strong> — triggered by the message boundary event
 *       {@code Event_1hkshcn} (message name: {@code user-cancel-msg},
 *       message ref: {@code Message_0k71eps}). The buyer explicitly cancels
 *       before the order is confirmed/delivered.</li>
 *   <li><strong>Contributor timeout / system error</strong> — triggered by the
 *       error boundary event {@code Event_0d416tw} which catches errors with
 *       error code {@code ORDER_CANCELED} thrown from inside {@code confirmation-sub-process}
 *       (e.g., delivering-to-agency timeout, failed-to-deliver-to-agency end event).</li>
 * </ol>
 *
 * <p>BPMN variable contract:
 * <pre>
 *   Input:  subOrderId       (String)  — loop element set by multi-instance sub-process
 *           cancelErrorCode  (String)  — populated by Camunda from error boundary event variable
 *           cancelErrorMessage (String)— populated by Camunda from error boundary event variable
 *   Action: calls product-service to cancel the sub-order and restore stock
 * </pre>
 */
@Slf4j
@Component("cancelOrderDelegate")
@RequiredArgsConstructor
public class CancleOrderDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    @SuppressWarnings("unchecked")
    public void execute(DelegateExecution execution) {
        String subOrderIdStr = (String) execution.getVariable("subOrderId");
        String cancelErrorCode    = (String) execution.getVariable("cancelErrorCode");
        String cancelErrorMessage = (String) execution.getVariable("cancelErrorMessage");
        String cancelReason       = (String) execution.getVariable("cancelReason");

        if (subOrderIdStr == null) {
            log.warn("[buying-items] cancleOrderDelegate fired but subOrderId is null — skipping cancel. " +
                     "cancelErrorCode={}, cancelErrorMessage={}", cancelErrorCode, cancelErrorMessage);
            return;
        }

        UUID subOrderId = UUID.fromString(subOrderIdStr);

        if (cancelErrorCode != null) {
            log.warn("[buying-items] Sub-order cancelled due to error: subOrderId={}, code={}, message={}",
                    subOrderId, cancelErrorCode, cancelErrorMessage);
        } else {
            log.info("[buying-items] Sub-order cancelled by user: subOrderId={}", subOrderId);
        }

        // Adjust sub-order and all snapshot statuses in Camunda process variables
        execution.setVariable("suborder_status_" + subOrderIdStr, "CANCELLED");

        java.util.List<java.util.Map<String, Object>> snapshots =
                (java.util.List<java.util.Map<String, Object>>) execution.getVariable("snapshots_" + subOrderIdStr);
        if (snapshots != null) {
            for (java.util.Map<String, Object> snapshot : snapshots) {
                String snapshotId = (String) snapshot.get("snapshotId");
                // Only cancel snapshots that are still active (don't overwrite terminal states)
                String currentStatus = (String) execution.getVariable("snapshot_status_" + snapshotId);
                if (currentStatus == null ||
                    (!currentStatus.equals("COMPLETED") &&
                     !currentStatus.equals("RETURNED") &&
                     !currentStatus.equals("REJECTED"))) {
                    execution.setVariable("snapshot_status_" + snapshotId, "CANCELLED");
                }
            }
        }

        String reason = cancelReason != null ? cancelReason : cancelErrorMessage;
        productClient.cancelSubOrder(subOrderId, reason);

        log.info("[buying-items] Sub-order cancel completed: subOrderId={}", subOrderId);
    }
}
