package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;
import org.example.ticketservice.application.usecase.CancelSubOrderUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelSubOrderService implements CancelSubOrderUseCase {

    private static final String CANCEL_MESSAGE = "user-cancel-msg";
    private static final Set<String> CANCELLABLE_STATUSES = Set.of("PENDING", "PACKING");

    private final RuntimeService runtimeService;

    @Override
    public void cancel(UUID subOrderId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }

        String subOrderIdString = subOrderId.toString();

        List<Execution> subscriptions = runtimeService.createExecutionQuery()
                .messageEventSubscriptionName(CANCEL_MESSAGE)
                .list()
                .stream()
                .filter(execution -> subOrderIdString.equals(
                        runtimeService.getVariable(execution.getId(), "subOrderId")))
                .toList();

        if (subscriptions.size() != 1) {
            throw new IllegalStateException(
                    "Expected one cancellation subscription for subOrderId=" + subOrderId
                            + " but found " + subscriptions.size());
        }

        Execution subscription = subscriptions.getFirst();
        String currentStatus = (String) runtimeService.getVariable(
                subscription.getId(), "suborder_status_" + subOrderIdString);
        if (!CANCELLABLE_STATUSES.contains(currentStatus)) {
            throw new IllegalStateException(
                    "Sub-order can only be cancelled before delivery starts: subOrderId="
                            + subOrderId + ", status=" + currentStatus);
        }

        runtimeService.messageEventReceived(
                CANCEL_MESSAGE,
                subscription.getId(),
                Map.of("cancelReason", reason.trim())
        );

        log.info("[buying-items] Sub-order cancelled before delivery: subOrderId={}, status={}, reason={}",
                subOrderId, currentStatus, reason);
    }
}
