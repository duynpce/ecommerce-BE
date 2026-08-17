package org.example.ticketservice.infrastructure.product;

import org.example.ticketservice.application.command.CreateProductReviewCommand;
import org.example.ticketservice.infrastructure.product.dto.SubOrderDto;
import org.example.ticketservice.infrastructure.product.dto.CancelSubOrderRequest;
import org.example.ticketservice.infrastructure.product.dto.UpdateTransactionStatusRequest;
import org.example.ticketservice.infrastructure.product.dto.UpdateSubOrderStatusRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;
import java.util.UUID;

/**
 * Spring HTTP Interface client for product-service state-transition endpoints.
 * Covers both legacy transaction-level operations and the new sub-order-level
 * operations used by the multi-instance buying-items-procedure BPMN.
 */
@HttpExchange
public interface ProductHttpClient {

    // ── Transaction-level (legacy / returning-items-procedure) ────────────────

    @PatchExchange("/api/v1/products/transactions/{id}/deliver")
    void deliver(@PathVariable UUID id);

    @PatchExchange("/api/v1/products/transactions/{id}/complete")
    void complete(
            @PathVariable UUID id,
            @RequestBody UpdateTransactionStatusRequest request);

    @PostExchange("/api/v1/products/transactions/{id}/return")
    void returnTransaction(@PathVariable UUID id);

    // ── Sub-order level (buying-items-procedure multi-instance) ───────────────

    @PatchExchange("/api/v1/products/sub-orders/{id}/approve")
    void approveSubOrder(@PathVariable UUID id);

    @PatchExchange("/api/v1/products/sub-orders/{id}/reject")
    void rejectSubOrder(@PathVariable UUID id);

    @PatchExchange("/api/v1/products/sub-orders/{id}/cancel")
    void cancelSubOrder(
            @PathVariable UUID id,
            @RequestBody CancelSubOrderRequest request);

    @PatchExchange("/api/v1/products/sub-orders/{id}/handoff")
    void handoffSubOrder(@PathVariable UUID id);

    @PatchExchange("/api/v1/products/sub-orders/{id}/items/{snapshotId}/deliver")
    void deliverSnapshot(
            @PathVariable UUID id,
            @PathVariable UUID snapshotId);

    @PatchExchange("/api/v1/products/sub-orders/{id}/complete")
    void completeSubOrder(
            @PathVariable UUID id,
            @RequestBody UpdateSubOrderStatusRequest request);

    /** Returns the list of sub-order IDs for the given transaction (used to init multi-instance loop). */
    @GetExchange("/api/v1/products/sub-orders/transaction/{transactionId}/ids")
    List<String> getSubOrderIds(@PathVariable UUID transactionId);

    /** Returns full sub-orders with product snapshots for a transaction. */
    @GetExchange("/api/v1/products/sub-orders/transaction/{transactionId}")
    List<SubOrderDto> getSubOrdersByTransactionId(@PathVariable UUID transactionId);

    /** Updates product snapshot status directly for snapshot-level sync/fallback. */
    @PatchExchange("/api/v1/products/sub-orders/{id}/items/{snapshotId}/snapshot-status")
    void updateSnapshotStatus(@PathVariable("id") UUID subOrderId, @PathVariable("snapshotId") UUID snapshotId, @RequestParam("status") String status);

    @PatchExchange("/api/v1/products/sub-orders/{id}/items/{snapshotId}/isReviewed")
    void markSnapshotIsReviewed(@PathVariable("id") UUID subOrderId, @PathVariable("snapshotId") UUID snapshotId, @RequestParam("isReviewed") Boolean isReviewed);

    @PostExchange("/api/v1/products/reviews/create")
    void createProductReview(@RequestBody CreateProductReviewCommand command);
}
