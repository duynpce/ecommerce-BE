package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message End Event delegate: {@code product returned} ({@code Event_1hd9vqw}),
 * registered as {@code ${startReturnProcessDelegate}}.
 *
 * <p>BPMN context: fires when the exclusive gateway routes to "product returned"
 * — i.e., when {@code status == "RETURNED"} or {@code retry >= 3}.
 *
 * <p>Responsibilities:
 * <ol>
 *   <li>Read the current sub-order context from Camunda process variables:
 *       {@code currentSnapshotId}, {@code shopId_&lt;subOrderId&gt;}, {@code transactionId},
 *       and {@code subOrderId}.</li>
 *   <li>Start a new {@code returning-items-procedure} Camunda process instance,
 *       passing all required context variables so the return process can operate
 *       independently.</li>
 * </ol>
 */
@Slf4j
@Component("startReturnProcessDelegate")
@RequiredArgsConstructor
public class StartReturnProcessDelegate implements JavaDelegate {

    private static final String RETURN_PROCESS_KEY = "returning-items-procedure";

    private final RuntimeService runtimeService;

    @Override
    @SuppressWarnings("unchecked")
    public void execute(DelegateExecution execution) {
        String subOrderIdStr     = (String) execution.getVariable("subOrderId");
        String transactionIdStr  = (String) execution.getVariable("transactionId");
        String currentSnapshotId = (String) execution.getVariable("currentSnapshotId");
        if (currentSnapshotId == null) {
            currentSnapshotId = (String) execution.getVariable("snapshotId");
        }

        // Retrieve shopId: stored per-sub-order as "shopId_<subOrderId>" or globally as "shopId"
        String shopIdStr = (String) execution.getVariable("shopId_" + subOrderIdStr);
        if (shopIdStr == null) {
            shopIdStr = (String) execution.getVariable("shopId");
        }

        // If shopId is still null, try to find it from snapshot map
        if (shopIdStr == null) {
            List<Map<String, Object>> snapshots =
                    (List<Map<String, Object>>) execution.getVariable("snapshots_" + subOrderIdStr);
            if (snapshots != null && !snapshots.isEmpty()) {
                shopIdStr = (String) snapshots.get(0).get("shopId");
            }
        }

        String contributorIdStr = (String) execution.getVariable("contributorId_" + subOrderIdStr);
        if (contributorIdStr == null) {
            List<Map<String, Object>> snapshots =
                    (List<Map<String, Object>>) execution.getVariable("snapshots_" + subOrderIdStr);
            if (snapshots != null && !snapshots.isEmpty()) {
                contributorIdStr = (String) snapshots.get(0).get("contributorId");
            }
        }
        if (contributorIdStr == null) {
            throw new IllegalStateException(
                    "Cannot start a return without the owning contributor: subOrderId=" + subOrderIdStr);
        }

        log.info("[buying-items] Starting return process: subOrderId={}, transactionId={}, " +
                        "snapshotId={}, shopId={}",
                subOrderIdStr, transactionIdStr, currentSnapshotId, shopIdStr);

        // Build process variables for the returning-items-procedure
        Map<String, Object> returnVariables = new HashMap<>();
        returnVariables.put("subOrderId",    subOrderIdStr);
        returnVariables.put("transactionId", transactionIdStr);
        returnVariables.put("snapshotId",    currentSnapshotId);
        returnVariables.put("shopId",        shopIdStr);
        returnVariables.put("contributorId", contributorIdStr);
        Map<String, Object> currentSnapshot =
                (Map<String, Object>) execution.getVariable("snapshot_" + currentSnapshotId);
        returnVariables.put("productName", currentSnapshot != null ? currentSnapshot.get("name") : null);
        // The return and its called delivery process intentionally share one
        // retry counter so the three-attempt limit applies across both shipper
        // and contributor-reported failures.
        returnVariables.put("retry",          0);
        returnVariables.put("returnRetry",    0);
        returnVariables.put("returnProcess",  true);
        returnVariables.put("deliveryOutcome", "PENDING");
        returnVariables.put("status",         "PENDING");

        // Use subOrderId as business key so the return process can be looked up by sub-order
        String businessKey = subOrderIdStr != null ? subOrderIdStr : transactionIdStr;
        boolean returnAlreadyActive = !runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(RETURN_PROCESS_KEY)
                .processInstanceBusinessKey(businessKey)
                .variableValueEquals("snapshotId", currentSnapshotId)
                .active()
                .list()
                .isEmpty();
        if (returnAlreadyActive) {
            log.info("[buying-items] Return process already active; duplicate start skipped: "
                            + "businessKey={}, snapshotId={}",
                    businessKey, currentSnapshotId);
            execution.setVariable("deliveryOutcome", "RETURN_REQUIRED");
            execution.setVariable("status", "RETURNED");
            return;
        }

        runtimeService.startProcessInstanceByKey(RETURN_PROCESS_KEY, businessKey, returnVariables);
        execution.setVariable("deliveryOutcome", "RETURN_REQUIRED");
        execution.setVariable("status", "RETURNED");

        log.info("[buying-items] Return process '{}' started: businessKey={}, snapshotId={}, " +
                        "shopId={}, transactionId={}",
                RETURN_PROCESS_KEY, businessKey, currentSnapshotId, shopIdStr, transactionIdStr);
    }
}
