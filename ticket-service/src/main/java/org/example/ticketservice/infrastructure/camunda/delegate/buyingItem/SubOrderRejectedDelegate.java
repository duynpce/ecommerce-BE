package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.RuntimeService;
import org.example.ticketservice.application.client.ProductClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Rejects only the current sub-order and restores its reserved stock. */
@Slf4j
@Component("subOrderRejectedDelegate")
@RequiredArgsConstructor
public class SubOrderRejectedDelegate implements JavaDelegate {

    private final ProductClient productClient;
    private final RuntimeService runtimeService;

    @Override
    @SuppressWarnings("unchecked")
    public void execute(DelegateExecution execution) {
        String subOrderIdValue = (String) execution.getVariable("subOrderId");
        if (subOrderIdValue == null || subOrderIdValue.isBlank()) {
            subOrderIdValue = (String) execution.getVariable("rejectedSubOrderId");
        }
        if (subOrderIdValue == null || subOrderIdValue.isBlank()) {
            throw new IllegalStateException(
                    "[buying-items] Missing rejected sub-order ID in process instance "
                            + execution.getProcessInstanceId());
        }

        UUID subOrderId = UUID.fromString(subOrderIdValue);
        String suppliedReason = (String) execution.getVariable("rejectionReason");
        String reason = suppliedReason == null || suppliedReason.isBlank()
                ? "Contributor confirmation timed out."
                : "Rejected by contributor: " + suppliedReason.trim();
        execution.setVariable("suborder_status_" + subOrderIdValue, "REJECTED");
        runtimeService.setVariable(
                execution.getProcessInstanceId(), "terminalSubOrderId", subOrderIdValue);
        runtimeService.setVariable(
                execution.getProcessInstanceId(), "terminalReason", reason);

        List<Map<String, Object>> snapshots =
                (List<Map<String, Object>>) execution.getVariable("snapshots_" + subOrderIdValue);
        if (snapshots != null) {
            for (Map<String, Object> snapshot : snapshots) {
                String snapshotId = (String) snapshot.get("snapshotId");
                execution.setVariable("snapshot_status_" + snapshotId, "REJECTED");
            }
        }

        productClient.rejectSubOrder(subOrderId, reason);
        log.info("[buying-items] Sub-order rejected and stock restored: subOrderId={}", subOrderId);
    }
}
