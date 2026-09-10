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
import java.util.Set;
import java.util.UUID;

/** Cancels only the current sub-order and restores its unfulfilled stock. */
@Slf4j
@Component("subOrderCanceledDelegate")
@RequiredArgsConstructor
public class SubOrderCanceledDelegate implements JavaDelegate {

    private static final Set<String> TERMINAL_SNAPSHOT_STATUSES =
            Set.of("COMPLETED", "RETURNED", "REJECTED", "CANCELLED");

    private final ProductClient productClient;
    private final RuntimeService runtimeService;

    @Override
    @SuppressWarnings("unchecked")
    public void execute(DelegateExecution execution) {
        String subOrderIdValue = (String) execution.getVariable("subOrderId");
        if (subOrderIdValue == null || subOrderIdValue.isBlank()) {
            subOrderIdValue = (String) execution.getVariable("cancelledSubOrderId");
        }
        if (subOrderIdValue == null || subOrderIdValue.isBlank()) {
            throw new IllegalStateException(
                    "[buying-items] Missing cancelled sub-order ID in process instance "
                            + execution.getProcessInstanceId());
        }

        UUID subOrderId = UUID.fromString(subOrderIdValue);
        execution.setVariable("suborder_status_" + subOrderIdValue, "CANCELLED");

        List<Map<String, Object>> snapshots =
                (List<Map<String, Object>>) execution.getVariable("snapshots_" + subOrderIdValue);
        if (snapshots != null) {
            for (Map<String, Object> snapshot : snapshots) {
                String snapshotId = (String) snapshot.get("snapshotId");
                String currentStatus =
                        (String) execution.getVariable("snapshot_status_" + snapshotId);
                if (currentStatus == null || !TERMINAL_SNAPSHOT_STATUSES.contains(currentStatus)) {
                    execution.setVariable("snapshot_status_" + snapshotId, "CANCELLED");
                }
            }
        }

        String reason = (String) execution.getVariable("cancelReason");
        if (reason == null || reason.isBlank()) {
            reason = (String) execution.getVariable("cancelErrorMessage");
        }
        if (reason == null || reason.isBlank()) {
            reason = Boolean.FALSE.equals(execution.getVariable("deliveredToAgency"))
                    ? "Contributor reported that the sub-order was not delivered to the agency."
                    : "Agency handoff confirmation timed out.";
        }
        runtimeService.setVariable(
                execution.getProcessInstanceId(), "terminalSubOrderId", subOrderIdValue);
        runtimeService.setVariable(
                execution.getProcessInstanceId(), "terminalReason", reason);
        productClient.cancelSubOrder(subOrderId, reason);
        log.info("[buying-items] Sub-order cancelled: subOrderId={}", subOrderId);
    }
}
