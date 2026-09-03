package org.example.ticketservice.application.client;

import org.example.ticketservice.application.command.CreateProductReviewCommand;
import org.example.ticketservice.domain.constant.TransactionStatus;
import org.example.ticketservice.domain.constant.SubOrderStatus;
import org.example.ticketservice.infrastructure.product.dto.SubOrderDto;

import java.util.List;
import java.util.UUID;

/**
 * Port for calling product-service state-transition endpoints during
 * the buying-items-procedure and returning-products Camunda processes.
 *
 * <p>Sub-order operations (approve/reject/cancel/deliver/complete) are used
 * by the updated multi-instance BPMN which operates at sub-order granularity.
 * The returning-items-procedure also operates on the individual snapshot in its
 * owning sub-order.
 */
public interface ProductClient {

    /** Transaction-level: DELIVERED → COMPLETED */
    default void complete(UUID transactionId, TransactionStatus status) {
        complete(transactionId, status, null, null);
    }

    void complete(
            UUID transactionId,
            TransactionStatus status,
            String reason,
            UUID triggerSubOrderId);

    // ── Sub-order level (buying-items-procedure multi-instance) ───────────────

    /** Sub-order: PENDING → snapshots PACKING (contributor approved) */
    void approveSubOrder(UUID subOrderId);

    /** Sub-order: PENDING → REJECTED, stock restored (contributor rejected) */
    void rejectSubOrder(UUID subOrderId, String reason);

    /** Sub-order: any non-terminal → CANCELLED, stock restored (user cancel / timeout) */
    void cancelSubOrder(UUID subOrderId, String reason);

    /** Whole sub-order carrier handoff: snapshots PACKING → DELIVERING. */
    void handoffSubOrder(UUID subOrderId);

    /** Persists a non-terminal sub-order state driven by the delivery workflow. */
    void updateSubOrderStatus(UUID subOrderId, SubOrderStatus status);

    /** One snapshot finishes delivery and records deliveredAt. */
    void deliverSnapshot(UUID subOrderId, UUID snapshotId);

    /** One returned snapshot reaches its contributor and has its stock restored. */
    void returnSnapshot(UUID subOrderId, UUID snapshotId);

    /** Writes the terminal status calculated from Camunda snapshot state. */
    void completeSubOrder(UUID subOrderId, SubOrderStatus status);

    /** Creates a product review in product-service for a delivered snapshot. */
    void createProductReview(CreateProductReviewCommand command);
    /**
     * Fetch the ordered list of sub-order IDs for a given transaction.
     * Used by {@code CreateTransactionAndSubOderDelegate} to populate the
     * multi-instance collection variable.
     */
    List<String> getSubOrderIds(UUID transactionId);

    /**
     * Fetch full sub-orders (including product snapshots) for a given transaction.
     * Used to populate snapshot details as Camunda process variables for status sync / fallback.
     */
    List<SubOrderDto> getSubOrdersByTransactionId(UUID transactionId);

    /**
     * Update an individual product snapshot's status.
     * Used for snapshot-level sync and fallback synchronization.
     */
    void updateSnapshotStatus(UUID subOrderId, UUID snapshotId, String status);
}
