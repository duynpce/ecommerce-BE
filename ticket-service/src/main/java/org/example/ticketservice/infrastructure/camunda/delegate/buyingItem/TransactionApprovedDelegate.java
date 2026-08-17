package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Service task: {@code approve order} ({@code approve-order}) inside the
 * confirmation sub-process {@code confirmation-sub-process}.
 *
 * <p>Fires on the "order approved" branch after the contributor completes
 * the "confirm products of sub-order" user task ({@code confirm-products-of-sub-order})
 * with {@code approve = true}.
 *
 * <p>Operates at <em>sub-order</em> granularity: the multi-instance loop
 * exposes the current sub-order's ID via the {@code subOrderId} process variable.
 * Transitions sub-order item snapshots: PENDING → PACKING in product-service.
 */
@Slf4j
@Component("transactionApprovedDelegate")
@RequiredArgsConstructor
public class TransactionApprovedDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    @SuppressWarnings("unchecked")
    public void execute(DelegateExecution execution) {
        String subOrderIdStr = (String) execution.getVariable("subOrderId");
        UUID subOrderId = UUID.fromString(subOrderIdStr);

        execution.setVariable("suborder_status_" + subOrderIdStr, "PACKING");

        java.util.List<java.util.Map<String, Object>> snapshots =
                (java.util.List<java.util.Map<String, Object>>) execution.getVariable("snapshots_" + subOrderIdStr);
        if (snapshots != null) {
            for (java.util.Map<String, Object> snapshot : snapshots) {
                String snapshotId = (String) snapshot.get("snapshotId");
                execution.setVariable("snapshot_status_" + snapshotId, "PACKING");
            }
        }

        productClient.approveSubOrder(subOrderId);

        log.info("[buying-items] Sub-order approved (snapshots -> PACKING): subOrderId={}", subOrderId);
    }
}
