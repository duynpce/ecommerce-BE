package org.example.ticketservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ticketservice.application.command.CreateProductReviewCommand;
import org.example.ticketservice.application.usecase.ConfirmDeliveryUseCase;
import org.example.ticketservice.application.usecase.CancelSubOrderUseCase;
import org.example.ticketservice.application.usecase.ConfirmReturnUseCase;
import org.example.ticketservice.application.usecase.ConfirmShippedUseCase;
import org.example.ticketservice.application.usecase.ConfirmTransactionUseCase;
import org.example.ticketservice.application.usecase.CreateProductReviewUseCase;
import org.example.ticketservice.application.usecase.StartBuyingProcedureUseCase;
import org.example.ticketservice.infrastructure.web.dto.ConfirmDeliveryRequest;
import org.example.ticketservice.infrastructure.web.dto.CancelSubOrderRequest;
import org.example.ticketservice.infrastructure.web.dto.ConfirmReturnRequest;
import org.example.ticketservice.infrastructure.web.dto.ConfirmTransactionRequest;
import org.example.ticketservice.infrastructure.web.dto.CreateProductReviewRequest;
import org.example.ticketservice.infrastructure.web.dto.ResponseDto;
import org.example.ticketservice.infrastructure.web.dto.StartBuyingProcedureRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Exposes HTTP endpoints for each step of the buying-items-procedure Camunda process.
 *
 * <p>Because the process uses a multi-instance sub-process (one instance per sub-order /
 * per merchant), steps 2-4 operate at <b>sub-order</b> granularity — each call targets
 * the specific Camunda user task that belongs to a single sub-order loop iteration.
 *
 * <p>Typical flow:
 * <pre>
 *  1. Buyer   -> POST  /transaction-tickets/start
 *                      (start process — pass transactionId from product-service)
 *
 *  2. Contrib -> POST  /transaction-tickets/sub-orders/{subOrderId}/confirm
 *                      (approve or reject one sub-order; one call per merchant)
 *
 *  3. Contrib -> POST  /transaction-tickets/sub-orders/{subOrderId}/shipped
 *                      (confirm handoff to carrier for one sub-order)
 *     [15-second mock-delivery timer fires automatically in Camunda]
 *
 *  4. Buyer   -> POST  /transaction-tickets/sub-orders/{subOrderId}/items/{snapshotId}/delivery
 *                      (RECEIVED | NOT_RECEIVED | RETURNED for one snapshot)
 *
 *  5. Contrib -> POST  /transaction-tickets/{transactionId}/confirm-return
 *                      (confirm the returned goods were received back — still transaction-scoped)
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/transaction-tickets")
@RequiredArgsConstructor
public class TransactionTicketController {

    private final StartBuyingProcedureUseCase  startBuyingProcedureUseCase;
    private final CancelSubOrderUseCase         cancelSubOrderUseCase;
    private final ConfirmTransactionUseCase    confirmTransactionUseCase;
    private final ConfirmShippedUseCase        confirmShippedUseCase;
    private final ConfirmDeliveryUseCase       confirmDeliveryUseCase;
    private final ConfirmReturnUseCase         confirmReturnUseCase;
    private final CreateProductReviewUseCase   createProductReviewUseCase;

    // -------------------------------------------------------------------------
    // Step 1 — Start the buying process (transaction-scoped)
    // -------------------------------------------------------------------------

    /**
     * Buyer starts the buying procedure after creating a transaction in product-service.
     * The transaction (and its sub-orders) must already exist in product-service before calling this.
     * <p>Authority: TRANSACTION:CREATE_SELF
     */
    @PostMapping("/start")
    public ResponseEntity<ResponseDto<Void>> start(
            @Valid @RequestBody StartBuyingProcedureRequest request) {

        log.info("Starting buying-items-procedure: transactionId={}, customerId={}",
                request.transactionId(), request.customerId());

        startBuyingProcedureUseCase.start(
                request.transactionId(),
                request.customerId(),
                request.subOrders()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseDto.success(null, "Buying procedure started successfully"));
    }


    // -------------------------------------------------------------------------
    // Steps 2-4 — Sub-order level (one call per merchant / sub-order)
    // -------------------------------------------------------------------------

    /**
     * Step 2 — Contributor approves or rejects a specific sub-order.
     * Completes the {@code confirm-products-of-sub-order} user task for the given sub-order.
     * In a multi-merchant cart, this must be called once per sub-order.
     * <p>Authority: TRANSACTION:WRITE_SELF (contributor role)
     *
     * @param subOrderId the sub-order ID (one per merchant in the cart)
     */
    @PostMapping("/sub-orders/{subOrderId}/confirm")
    @PreAuthorize("hasAuthority('TRANSACTION:WRITE_SELF')")
    public ResponseEntity<ResponseDto<Void>> confirmSubOrder(
            @PathVariable UUID subOrderId,
            @Valid @RequestBody ConfirmTransactionRequest request) {

        confirmTransactionUseCase.confirm(subOrderId, request.approve());

        String message = request.approve() ? "Sub-order approved" : "Sub-order rejected";
        log.info("Sub-order {}: subOrderId={}", request.approve() ? "approved" : "rejected", subOrderId);

        return ResponseEntity.ok(ResponseDto.success(null, message));
    }

    /**
     * Backward-compatible route for clients that still send a snapshot ID.
     * Rejection is a sub-order decision, so the snapshot ID is not used as the
     * update target and the normal Camunda rejection path is executed.
     */
    @PostMapping("/sub-orders/{subOrderId}/items/{snapshotId}/reject")
    @PreAuthorize("hasAuthority('TRANSACTION:WRITE_SELF')")
    public ResponseEntity<ResponseDto<Void>> rejectSubOrderFromSnapshotRoute(
            @PathVariable UUID subOrderId,
            @PathVariable UUID snapshotId) {

        confirmTransactionUseCase.confirm(subOrderId, false);
        log.info("Whole sub-order rejected from legacy snapshot route: subOrderId={}, snapshotId={}",
                subOrderId, snapshotId);
        return ResponseEntity.ok(ResponseDto.success(null, "Sub-order rejected"));
    }

    /**
     * Backward-compatible route for clients that still send a snapshot ID.
     * Cancellation goes through Camunda and cancels every active snapshot in
     * the sub-order.
     */
    @PostMapping("/sub-orders/{subOrderId}/items/{snapshotId}/cancel")
    @PreAuthorize("hasAuthority('TRANSACTION:WRITE_SELF') or hasAuthority('TRANSACTION:CREATE_SELF')")
    public ResponseEntity<ResponseDto<Void>> cancelSubOrderFromSnapshotRoute(
            @PathVariable UUID subOrderId,
            @PathVariable UUID snapshotId) {

        cancelSubOrderUseCase.cancel(
                subOrderId, "Cancellation requested through item " + snapshotId);
        log.info("Whole sub-order cancelled from legacy snapshot route: subOrderId={}, snapshotId={}",
                subOrderId, snapshotId);
        return ResponseEntity.ok(ResponseDto.success(null, "Sub-order cancelled"));
    }

    /**
     * Step 3 — Contributor confirms the sub-order's goods were handed to the transportation agency.
     * Completes the {@code confirm-delivery-to-transportation-agency} user task.
     * After this, the 15-second mock-delivery timer starts automatically in Camunda.
     * <p>Authority: TRANSACTION:WRITE_SELF (contributor role)
     *
     * @param subOrderId the sub-order ID
     */
    @PostMapping("/sub-orders/{subOrderId}/shipped")
    @PreAuthorize("hasAuthority('TRANSACTION:WRITE_SELF')")
    public ResponseEntity<ResponseDto<Void>> confirmShipped(
            @PathVariable UUID subOrderId) {

        confirmShippedUseCase.confirmShipped(subOrderId);

        log.info("Sub-order goods handed to agency: subOrderId={}", subOrderId);

        return ResponseEntity.ok(ResponseDto.success(null, "Sub-order goods handed to transportation agency"));
    }

    /** Contributor cancels a sub-order while it is packing. */
    @PostMapping("/sub-orders/{subOrderId}/cancel")
    @PreAuthorize("hasAuthority('TRANSACTION:WRITE_SELF')")
    public ResponseEntity<ResponseDto<Void>> cancelPackingSubOrder(
            @PathVariable UUID subOrderId,
            @Valid @RequestBody CancelSubOrderRequest request) {

        cancelSubOrderUseCase.cancel(subOrderId, request.reason());
        return ResponseEntity.ok(ResponseDto.success(null, "Sub-order cancelled"));
    }

    /**
     * Step 4 — Buyer confirms the delivery outcome for a specific snapshot.
     * Completes the {@code confirm-delivery-status} user task.
     * The {@code status} value drives the exclusive gateway:
     * <ul>
     *   <li>{@code RECEIVED}     -> sub-order COMPLETED then review-product user task opens</li>
     *   <li>{@code NOT_RECEIVED} -> retry delivery (up to 3 times, then forced return)</li>
     *   <li>{@code RETURNED}     -> return procedure call activity activated</li>
     * </ul>
     * <p>Authority: TRANSACTION:CREATE_SELF (buyer role)
     *
     * @param subOrderId the sub-order ID
     * @param snapshotId the product snapshot ID
     */
    @PostMapping("/sub-orders/{subOrderId}/items/{snapshotId}/delivery")
    @PreAuthorize("hasAuthority('TRANSACTION:CREATE_SELF')")
    public ResponseEntity<ResponseDto<Void>> confirmDelivery(
            @PathVariable UUID subOrderId,
            @PathVariable UUID snapshotId,
            @Valid @RequestBody ConfirmDeliveryRequest request) {

        confirmDeliveryUseCase.confirmDelivery(subOrderId, snapshotId, request.status());

        log.info("Delivery status recorded: subOrderId={}, snapshotId={}, status={}",
                subOrderId, snapshotId, request.status());

        return ResponseEntity.ok(ResponseDto.success(null,
                "Delivery status recorded: " + request.status()));
    }

    /**
     * Step 4b — Buyer creates a review for a delivered product snapshot.
     * Product-service persists the review and updates product/shop ratings; after
     * it succeeds, ticket-service completes the matching Camunda review task.
     */
    @PostMapping("/sub-orders/{subOrderId}/reviews")
    @PreAuthorize("hasAuthority('REVIEW:WRITE_SELF')")
    public ResponseEntity<ResponseDto<Void>> createProductReview(
            @PathVariable UUID subOrderId,
            @Valid @RequestBody CreateProductReviewRequest request) {

        CreateProductReviewCommand command = new CreateProductReviewCommand(
                request.productId(),
                request.transactionId(),
                request.snapshotId(),
                request.rating(),
                request.comment());

        createProductReviewUseCase.create(subOrderId, command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.success(null, "Product review created successfully"));
    }


    // -------------------------------------------------------------------------
    // Step 5 — Return confirmation (transaction-scoped)
    // -------------------------------------------------------------------------

    /**
     * Step 5 — Contributor confirms whether the returned product was received back.
     * Completes the return-confirmation user task in the returning-items Camunda sub-process.
     * <p>Authority: TRANSACTION:WRITE_SELF (contributor role)
     */
    @PostMapping("/{snapshotId}/confirm-return")
    @PreAuthorize("hasAuthority('TRANSACTION:WRITE_SELF')")
    public ResponseEntity<ResponseDto<Void>> confirmReturn(
            @PathVariable UUID snapshotId,
            @Valid @RequestBody ConfirmReturnRequest request) {

        confirmReturnUseCase.confirmReturn(snapshotId, request.received());

        String message = request.received()
                ? "Returned product received — stock restored"
                : "Returned product reported as not received";

        return ResponseEntity.ok(ResponseDto.success(null, message));
    }
}
