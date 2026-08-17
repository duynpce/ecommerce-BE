package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.example.ticketservice.infrastructure.product.dto.ProductSnapshotDto;
import org.example.ticketservice.infrastructure.product.dto.SubOrderDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Execution listener and Java delegate registered in the buying-items-procedure.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Captures the Camunda process instance ID ({@code processInstanceId}).</li>
 *   <li>Fetches current snapshot statuses from {@code product-service}.</li>
 *   <li><b>Targeted check</b>: when {@code currentSnapshotId} is set (by {@code deliver-the-product}
 *       before confirm-delivery tasks), compares Camunda vs DB status for that specific snapshot
 *       before allowing the step to complete. Corrects DB if mismatched.</li>
 *   <li><b>Full scan</b>: compares every snapshot's Camunda status vs DB status and corrects
 *       any mismatch before completing the step.</li>
 * </ul>
 */
@Slf4j
@Component("syncDbStatusDelegate")
@RequiredArgsConstructor
public class SyncDbStatusDelegate implements ExecutionListener, JavaDelegate {

    private final ProductClient productClient;

    @Override
    public void notify(DelegateExecution execution) {
//        checkAndSyncSnapshotStatuses(execution);
    }

    @Override
    public void execute(DelegateExecution execution) {
//        checkAndSyncSnapshotStatuses(execution);
    }

    private void checkAndSyncSnapshotStatuses(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        String transactionIdStr  = (String) execution.getVariable("transactionId");
        String currentSnapshotIdStr = (String) execution.getVariable("currentSnapshotId");

        execution.setVariable("processInstanceId", processInstanceId);

        if (transactionIdStr == null) {
            return;
        }

        UUID transactionId = UUID.fromString(transactionIdStr);
        List<SubOrderDto> subOrdersFromDb = productClient.getSubOrdersByTransactionId(transactionId);

        if (subOrdersFromDb == null || subOrdersFromDb.isEmpty()) {
            return;
        }

        // ── Targeted check: the snapshot currently in progress ───────────────────
        if (currentSnapshotIdStr != null) {
            String camundaStatusForCurrent = (String) execution.getVariable("snapshot_status_" + currentSnapshotIdStr);
            outer:
            for (SubOrderDto subOrder : subOrdersFromDb) {
                if (subOrder.items() != null) {
                    for (ProductSnapshotDto snapshot : subOrder.items()) {
                        if (currentSnapshotIdStr.equals(snapshot.id().toString())) {
                            String dbStatus = snapshot.status() != null ? snapshot.status() : "PENDING";
                            if (camundaStatusForCurrent != null && !camundaStatusForCurrent.equalsIgnoreCase(dbStatus)) {
                                log.warn("[syncDbStatusDelegate] Current snapshot status mismatch — " +
                                                "snapshotId={}, Camunda={}, DB={}. Correcting DB.",
                                        currentSnapshotIdStr, camundaStatusForCurrent, dbStatus);
                                productClient.updateSnapshotStatus(subOrder.id(), snapshot.id(), camundaStatusForCurrent);
                                throw new IllegalStateException(
                                        "Synchronous error: Current snapshot status mismatch, Status has been synchronized. Please try again later.");
                            }
                            break outer;
                        }
                    }
                }
            }
        }

        boolean mismatchFound = false;

        for (SubOrderDto subOrder : subOrdersFromDb) {
            UUID subOrderId = subOrder.id();
            if (subOrder.items() != null) {
                for (ProductSnapshotDto snapshotFromDb : subOrder.items()) {
                    String snapshotIdStr = snapshotFromDb.id().toString();
                    String dbStatus = snapshotFromDb.status() != null ? snapshotFromDb.status() : "PENDING";

                    String camundaStatus = (String) execution.getVariable("snapshot_status_" + snapshotIdStr);

                    if (camundaStatus == null) {
                        execution.setVariable("snapshot_status_" + snapshotIdStr, dbStatus);
                    } else if (!camundaStatus.equalsIgnoreCase(dbStatus)) {
                        log.warn("[syncDbStatusDelegate] Status mismatch for snapshotId={}: Camunda={}, DB={}. Synchronizing DB to Camunda status.",
                                snapshotIdStr, camundaStatus, dbStatus);

                        productClient.updateSnapshotStatus(subOrderId, snapshotFromDb.id(), camundaStatus);
                        mismatchFound = true;
                    }
                }
            }
        }

        if (mismatchFound) {
            log.error("[syncDbStatusDelegate] Synchronous error detected for transactionId={}. Product service status updated to match Camunda.", transactionIdStr);
            throw new IllegalStateException("Synchronous error: Snapshot status mismatch detected between Camunda and product-service. Statuses have been synchronized to product-service. Please try again later.");
        }

        log.info("[syncDbStatusDelegate] Snapshot status check completed cleanly for transactionId={}", transactionIdStr);
    }
}
