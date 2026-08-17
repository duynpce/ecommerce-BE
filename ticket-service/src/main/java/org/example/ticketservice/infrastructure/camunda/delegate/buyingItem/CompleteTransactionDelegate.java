package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.example.ticketservice.domain.constant.TransactionStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Message End Event delegate: {@code transaction complete} ({@code ${completeTransactionDelegate}}).
 *
 * <p>BPMN Documentation:
 * "all of its sub order ended then complete the transaction"
 *
 * <p>Evaluates all sub-order statuses of the parent transaction in Camunda process variables,
 * computes the overall transaction ended status (COMPLETED, RETURNED, CANCELLED, REJECTED,
 * or PARTIALLY_RETURNED), stores it in Camunda process variables, and calls product-service to
 * complete/adjust the transaction status in DB.
 */
@Slf4j
@Component("completeTransactionDelegate")
@RequiredArgsConstructor
public class CompleteTransactionDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    @SuppressWarnings("unchecked")
    public void execute(DelegateExecution execution) {
        String transactionIdStr = (String) execution.getVariable("transactionId");
        if (transactionIdStr == null) {
            log.warn("[buying-items] completeTransaction delegate executed but transactionId is null");
            return;
        }

        List<String> subOrderIds = (List<String>) execution.getVariable("subOrderIds");
        if (subOrderIds == null || subOrderIds.isEmpty()) {
            throw new IllegalStateException(
                    "[buying-items] Cannot complete transaction without subOrderIds: " + transactionIdStr);
        }

        int completedCount = 0;
        int returnedCount  = 0;
        int cancelledCount = 0;
        int rejectedCount  = 0;
        int total = subOrderIds.size();

        for (String subOrderId : subOrderIds) {
            String subOrderStatus = (String) execution.getVariable("suborder_status_" + subOrderId);
            if (subOrderStatus == null) {
                throw new IllegalStateException(
                        "[buying-items] Missing final status for subOrderId=" + subOrderId);
            }

            switch (subOrderStatus) {
                case "COMPLETED" -> completedCount++;
                case "RETURNED"  -> returnedCount++;
                case "CANCELLED" -> cancelledCount++;
                case "REJECTED"  -> rejectedCount++;
                case "PARTIALLY_RETURNED" -> {
                    // Already terminal; any transaction containing one is partially returned.
                }
                default -> throw new IllegalStateException(
                        "[buying-items] Sub-order is not in a final status: "
                                + subOrderId + "=" + subOrderStatus);
            }
        }

        TransactionStatus transactionStatus;
        if (completedCount == total) {
            transactionStatus = TransactionStatus.COMPLETED;
        } else if (returnedCount == total) {
            transactionStatus = TransactionStatus.RETURNED;
        } else if (cancelledCount == total) {
            transactionStatus = TransactionStatus.CANCELLED;
        } else if (rejectedCount == total) {
            transactionStatus = TransactionStatus.REJECTED;
        } else {
            transactionStatus = TransactionStatus.PARTIALLY_RETURNED;
        }

        execution.setVariable("transaction_status_" + transactionIdStr, transactionStatus.name());
        log.info("[buying-items] Evaluated transaction status in Camunda: transactionId={}, status={}",
                transactionIdStr, transactionStatus);

        UUID transactionId = UUID.fromString(transactionIdStr);
        productClient.complete(transactionId, transactionStatus);

        log.info("[buying-items] Transaction status synced to product-service: transactionId={}, status={}",
                transactionId, transactionStatus);
    }
}
