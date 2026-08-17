package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Service task: {@code reject order} ({@code reject-order}) inside the
 * confirmation sub-process {@code confirmation-sub-process}.
 *
 * <p>Fires on the "product rejected" branch after the contributor completes
 * the "confirm products of sub-order" user task ({@code confirm-products-of-sub-order})
 * with {@code approve = false}, <em>or</em> when the confirmation timeout
 * boundary event fires (auto-reject after PT1M).
 *
 * <p>Operates at <em>sub-order</em> granularity via the {@code subOrderId}
 * loop variable. Transitions sub-order: PENDING -> CANCELLED in product-service;
 * reserved stock is restored.
 */
@Slf4j
@Component("transactionRejectedDelegate")
@RequiredArgsConstructor
public class TransactionRejectedDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    @SuppressWarnings("unchecked")
    public void execute(DelegateExecution execution) {
        String subOrderIdStr = (String) execution.getVariable("subOrderId");
        UUID subOrderId = UUID.fromString(subOrderIdStr);

        execution.setVariable("suborder_status_" + subOrderIdStr, "REJECTED");

        java.util.List<java.util.Map<String, Object>> snapshots =
                (java.util.List<java.util.Map<String, Object>>) execution.getVariable("snapshots_" + subOrderIdStr);
        if (snapshots != null) {
            for (java.util.Map<String, Object> snapshot : snapshots) {
                String snapshotId = (String) snapshot.get("snapshotId");
                execution.setVariable("snapshot_status_" + snapshotId, "REJECTED");
            }
        }

        productClient.rejectSubOrder(subOrderId);

        log.info("[buying-items] Sub-order rejected and stock restored: subOrderId={}", subOrderId);
    }
}
