package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.example.ticketservice.domain.constant.SubOrderStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Marks the current sub-order while the remaining shop parcels are pending. */
@Slf4j
@Component("markSubOrderWaitingForConsolidationDelegate")
@RequiredArgsConstructor
public class MarkSubOrderWaitingForConsolidationDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    public void execute(DelegateExecution execution) {
        Object value = execution.getVariable("subOrderId");
        if (!(value instanceof String subOrderIdValue) || subOrderIdValue.isBlank()) {
            throw new IllegalStateException(
                    "[buying-items] Missing subOrderId for consolidation transition");
        }

        UUID subOrderId = parseUuid(subOrderIdValue);
        productClient.updateSubOrderStatus(
                subOrderId, SubOrderStatus.WAITING_FOR_CONSOLIDATION);
        execution.setVariable(
                "suborder_status_" + subOrderIdValue,
                SubOrderStatus.WAITING_FOR_CONSOLIDATION.name());

        log.info("[buying-items] Sub-order is waiting for consolidation: subOrderId={}",
                subOrderId);
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "[buying-items] Invalid subOrderId=" + value, exception);
        }
    }
}
