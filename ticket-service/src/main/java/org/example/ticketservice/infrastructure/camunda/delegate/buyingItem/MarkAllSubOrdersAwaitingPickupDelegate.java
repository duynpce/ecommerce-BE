package org.example.ticketservice.infrastructure.camunda.delegate.buyingItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.ProductClient;
import org.example.ticketservice.domain.constant.SubOrderStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Marks every shop parcel ready once transaction-wide consolidation is complete. */
@Slf4j
@Component("markAllSubOrdersAwaitingPickupDelegate")
@RequiredArgsConstructor
public class MarkAllSubOrdersAwaitingPickupDelegate implements JavaDelegate {

    private final ProductClient productClient;

    @Override
    public void execute(DelegateExecution execution) {
        List<String> subOrderIds = requireSubOrderIds(execution);
        if (!Boolean.TRUE.equals(execution.getVariable("allSubOrderConfirmed"))) {
            throw new IllegalStateException(
                    "[buying-items] Cannot mark parcels awaiting pickup before all are confirmed");
        }

        for (String value : subOrderIds) {
            UUID subOrderId = parseUuid(value);
            productClient.updateSubOrderStatus(
                    subOrderId, SubOrderStatus.AWAITING_PICKUP);
            execution.setVariable(
                    "suborder_status_" + value, SubOrderStatus.AWAITING_PICKUP.name());
        }

        log.info("[buying-items] All sub-orders are awaiting pickup: count={}",
                subOrderIds.size());
    }

    @SuppressWarnings("unchecked")
    private List<String> requireSubOrderIds(DelegateExecution execution) {
        Object value = execution.getVariable("subOrderIds");
        if (!(value instanceof List<?> ids) || ids.isEmpty()
                || ids.stream().anyMatch(id -> !(id instanceof String))) {
            throw new IllegalStateException(
                    "[buying-items] Missing or invalid subOrderIds process variable");
        }
        return (List<String>) ids;
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
