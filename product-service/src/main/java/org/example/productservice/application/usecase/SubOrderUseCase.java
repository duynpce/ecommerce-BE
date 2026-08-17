package org.example.productservice.application.usecase;

import org.example.productservice.application.command.CreateSubOrderCommand;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateSubOrderCommand;
import org.example.productservice.application.criteria.SubOrderSearchCriteria;
import org.example.productservice.domain.constant.ProductSnapshotStatus;
import org.example.productservice.domain.constant.SubOrderStatus;
import org.example.productservice.domain.model.SubOrder;

import java.util.List;
import java.util.UUID;

public interface SubOrderUseCase {
    SubOrder create(CreateSubOrderCommand command);
    SubOrder findById(UUID id);
    List<SubOrder> findByTransactionId(UUID transactionId);
    List<SubOrder> findByShopId(UUID shopId);
    List<SubOrder> findByCustomerId(UUID customerId);
    SubOrder update(UpdateSubOrderCommand command);
    SubOrder updateStatus(UUID id, SubOrderStatus status);
    SubOrder updateSnapshotStatus(UUID subOrderId, UUID snapshotId, ProductSnapshotStatus status);
    void delete(UUID id);
    PageCommand<SubOrder> search(SubOrderSearchCriteria criteria);

    // ── Camunda-driven state transitions (called by ticket-service delegates) ──

    /** Contributor approved the sub-order: PENDING → PACKING (all snapshots → PACKING). */
    SubOrder approve(UUID id);

    /** Contributor rejected the sub-order: PENDING → REJECTED (stock restored). */
    SubOrder reject(UUID id);

    /** User or timeout cancelled the sub-order: any non-terminal state → CANCELLED (stock restored). */
    SubOrder cancel(UUID id, String reason);

    /** Contributor confirmed handoff to carrier: all PACKING snapshots → DELIVERING. */
    SubOrder handoff(UUID id);

    /** Carrier delivery completed for one snapshot: DELIVERING → awaiting confirmation. */
    SubOrder deliver(UUID id, UUID snapshotId);

    SubOrder markSnapshotIsReviewed(UUID id, UUID snapshotId, boolean isReviewed);

    /** Writes the terminal status calculated by ticket-service without changing snapshots. */
    SubOrder completeSubOrder(UUID id, SubOrderStatus status);

}
