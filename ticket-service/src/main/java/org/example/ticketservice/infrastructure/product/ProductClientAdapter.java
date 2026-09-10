package org.example.ticketservice.infrastructure.product;

import lombok.RequiredArgsConstructor;
import org.example.ticketservice.application.client.ProductClient;
import org.example.ticketservice.application.command.CreateProductReviewCommand;
import org.example.ticketservice.domain.constant.TransactionStatus;
import org.example.ticketservice.domain.constant.SubOrderStatus;
import org.example.ticketservice.infrastructure.product.dto.SubOrderDto;
import org.example.ticketservice.infrastructure.product.dto.CancelSubOrderRequest;
import org.example.ticketservice.infrastructure.product.dto.UpdateTransactionStatusRequest;
import org.example.ticketservice.infrastructure.product.dto.UpdateSubOrderStatusRequest;
import org.example.ticketservice.infrastructure.product.dto.TerminalStatusReasonRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Adapter that bridges the application-layer {@link ProductClient} port
 * to the Spring HTTP Interface {@link ProductHttpClient}.
 */
@Component
@RequiredArgsConstructor
public class ProductClientAdapter implements ProductClient {

    private final ProductHttpClient productHttpClient;

    @Override
    public void complete(UUID transactionId, TransactionStatus status) {
        complete(transactionId, status, null, null);
    }

    @Override
    public void complete(
            UUID transactionId,
            TransactionStatus status,
            String reason,
            UUID triggerSubOrderId) {
        productHttpClient.complete(
                transactionId,
                new UpdateTransactionStatusRequest(status, reason, triggerSubOrderId));
    }

    // ── Sub-order level ────────────────────────────────────────────────────────

    @Override
    public void approveSubOrder(UUID subOrderId) {
        productHttpClient.approveSubOrder(subOrderId);
    }

    @Override
    public void rejectSubOrder(UUID subOrderId, String reason) {
        productHttpClient.rejectSubOrder(subOrderId, new TerminalStatusReasonRequest(reason));
    }

    @Override
    public void cancelSubOrder(UUID subOrderId, String reason) {
        productHttpClient.cancelSubOrder(subOrderId, new CancelSubOrderRequest(reason));
    }

    @Override
    public void handoffSubOrder(UUID subOrderId) {
        productHttpClient.handoffSubOrder(subOrderId);
    }

    @Override
    public void updateSubOrderStatus(UUID subOrderId, SubOrderStatus status) {
        productHttpClient.updateSubOrderStatus(
                subOrderId, new UpdateSubOrderStatusRequest(status));
    }

    @Override
    public void deliverSnapshot(UUID subOrderId, UUID snapshotId) {
        productHttpClient.deliverSnapshot(subOrderId, snapshotId);
    }

    @Override
    public void returnSnapshot(UUID subOrderId, UUID snapshotId) {
        productHttpClient.returnSnapshot(subOrderId, snapshotId);
    }

    @Override
    public void completeSubOrder(UUID subOrderId, SubOrderStatus status) {
        productHttpClient.completeSubOrder(subOrderId, new UpdateSubOrderStatusRequest(status));
    }

    @Override
    public List<String> getSubOrderIds(UUID transactionId) {
        return productHttpClient.getSubOrderIds(transactionId);
    }

    @Override
    public List<SubOrderDto> getSubOrdersByTransactionId(UUID transactionId) {
        return productHttpClient.getSubOrdersByTransactionId(transactionId);
    }

    @Override
    public void updateSnapshotStatus(UUID subOrderId, UUID snapshotId, String status) {
        productHttpClient.updateSnapshotStatus(subOrderId, snapshotId, status);
    }


    @Override
    public void createProductReview(CreateProductReviewCommand command) {
        productHttpClient.createProductReview(command);
    }
}
