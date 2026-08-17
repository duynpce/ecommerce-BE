package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.example.ticketservice.application.usecase.StartBuyingProcedureUseCase;
import org.springframework.stereotype.Service;

import org.example.ticketservice.infrastructure.product.dto.ProductSnapshotDto;
import org.example.ticketservice.infrastructure.product.dto.SubOrderDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StartBuyingProcedureService implements StartBuyingProcedureUseCase {

    private static final String PROCESS_KEY = "buying-items-procedure";

    private final RuntimeService runtimeService;

    @Override
    public void start(UUID transactionId, UUID customerId, List<SubOrderDto> subOrders) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("transactionId",  transactionId.toString());

        if (customerId != null) {
            variables.put("customerId", customerId.toString());
        }
        if (subOrders == null || subOrders.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one sub-order is required to start the buying procedure");
        }

        List<String> subOrderIds = new ArrayList<>();
            List<String> allSnapshotIds = new ArrayList<>();
            List<Map<String, Object>> allSnapshots = new ArrayList<>();
            Map<String, List<Map<String, Object>>> subOrderSnapshotsMap = new HashMap<>();

            for (SubOrderDto subOrder : subOrders) {
                String subOrderIdStr = subOrder.id().toString();
                String shopIdStr     = subOrder.shopId() != null ? subOrder.shopId().toString() : null;
                subOrderIds.add(subOrderIdStr);

                // Store shopId per sub-order so delegates (e.g. StartReturnProcessDelegate) can look it up
                variables.put("shopId_" + subOrderIdStr, shopIdStr);

                List<Map<String, Object>> snapshotListForSubOrder = new ArrayList<>();
                List<String> snapshotIdsForSubOrder = new ArrayList<>();

                if (subOrder.items() != null) {
                    for (ProductSnapshotDto item : subOrder.items()) {
                        String snapshotIdStr = item.id().toString();
                        allSnapshotIds.add(snapshotIdStr);
                        snapshotIdsForSubOrder.add(snapshotIdStr);

                        Map<String, Object> snapshotMap = new HashMap<>();
                        snapshotMap.put("snapshotId", snapshotIdStr);
                        snapshotMap.put("subOrderId", subOrderIdStr);
                        snapshotMap.put("shopId", shopIdStr);
                        snapshotMap.put("transactionId", transactionId.toString());
                        snapshotMap.put("productId", item.productId() != null ? item.productId().toString() : null);
                        snapshotMap.put("name", item.name());
                        snapshotMap.put("price", item.price());
                        snapshotMap.put("quantity", item.quantity());
                        snapshotMap.put("status", item.status() != null ? item.status() : "PENDING");
                        snapshotMap.put("isReviewed", item.isReviewed());

                        allSnapshots.add(snapshotMap);
                        snapshotListForSubOrder.add(snapshotMap);

                        variables.put("snapshot_" + snapshotIdStr, snapshotMap);
                        variables.put("snapshot_active_" + snapshotIdStr, true);
                        variables.put("snapshot_status_" + snapshotIdStr, item.status() != null ? item.status() : "PENDING");
                        variables.put("snapshot_reviewed_" + snapshotIdStr,
                                Boolean.TRUE.equals(item.isReviewed()));
                    }
                }

                subOrderSnapshotsMap.put(subOrderIdStr, snapshotListForSubOrder);
                variables.put("snapshots_" + subOrderIdStr, snapshotListForSubOrder);
                variables.put("snapshotIds_" + subOrderIdStr, snapshotIdsForSubOrder);
                variables.put("suborder_status_" + subOrderIdStr, subOrder.status() != null ? subOrder.status() : "PENDING");
            }

            // The delegate validates this prepared list and publishes "subOrderIds".
            // That BPMN collection creates one multi-instance sub-process per ID.
            variables.put("subOrderIdsToCreate", subOrderIds);
            variables.put("snapshotIds", allSnapshotIds);
            variables.put("allSnapshots", allSnapshots);
            variables.put("subOrderSnapshotsMap", subOrderSnapshotsMap);

        // Use transactionId as the business key so tasks can be looked up by it
        runtimeService.startProcessInstanceByKey(PROCESS_KEY, transactionId.toString(), variables);

        log.info("[buying-items] Started Camunda process '{}' for transactionId={} with {} sub-orders",
                PROCESS_KEY, transactionId, subOrders != null ? subOrders.size() : 0);
    }
}
