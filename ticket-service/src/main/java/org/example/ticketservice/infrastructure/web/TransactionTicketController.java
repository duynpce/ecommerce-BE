package org.example.ticketservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ticketservice.application.client.TokenGeneratorClient;
import org.example.ticketservice.application.usecase.ConfirmDeliveryUseCase;
import org.example.ticketservice.application.usecase.ConfirmReturnUseCase;
import org.example.ticketservice.application.usecase.ConfirmShippedUseCase;
import org.example.ticketservice.application.usecase.ConfirmTransactionUseCase;
import org.example.ticketservice.application.usecase.StartBuyingProcedureUseCase;
import org.example.ticketservice.infrastructure.web.dto.ConfirmDeliveryRequest;
import org.example.ticketservice.infrastructure.web.dto.ConfirmReturnRequest;
import org.example.ticketservice.infrastructure.web.dto.ConfirmTransactionRequest;
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
 * Typical flow:
 *  1. Buyer  → POST   /transaction-tickets/start                   (start process)
 *  2. Contrib → POST  /transaction-tickets/{id}/confirm            (approve or reject)
 *  3. Contrib → POST  /transaction-tickets/{id}/shipped            (confirm handoff to carrier)
 *     [30-second timer fires automatically in Camunda]
 *  4. Buyer  → POST   /transaction-tickets/{id}/delivery           (RECEIVED | NOT_RECEIVED | RETURNED)
 *  5. Contrib → POST  /transaction-tickets/{id}/confirm-return     (confirm return received back)
 */
@Slf4j
@RestController
@RequestMapping("/transaction-tickets")
@RequiredArgsConstructor
public class TransactionTicketController {

    private final StartBuyingProcedureUseCase  startBuyingProcedureUseCase;
    private final ConfirmTransactionUseCase    confirmTransactionUseCase;
    private final ConfirmShippedUseCase        confirmShippedUseCase;
    private final ConfirmDeliveryUseCase       confirmDeliveryUseCase;
    private final ConfirmReturnUseCase         confirmReturnUseCase;


    /**
     * Step 1 — Buyer starts the buying process after creating a transaction in product-service.
     * The transaction must already exist in product-service before calling this endpoint.
     * Requires: TRANSACTION:CREATE_SELF
     */
    @PostMapping("/start")
    public ResponseEntity<ResponseDto<Void>> start(
            @Valid @RequestBody StartBuyingProcedureRequest request
          ) {
        log.info("Starting buying-items-procedure: transactionId={}, contributorId={}, customerId={}", request.transactionId(), request.contributorId(), request.customerId());

        startBuyingProcedureUseCase.start(
                request.transactionId(),
                request.contributorId(),
                request.customerId()

        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseDto.success(null, "Buying procedure started successfully"));
    }

    /**
     * Step 2 — Contributor approves or rejects the transaction.
     * Completes the "confirm-the-transaction" user task.
     * Requires: TRANSACTION:UPDATE_SELF (contributor role)
     */
    @PostMapping("/{transactionId}/confirm")
    @PreAuthorize("hasAuthority('TRANSACTION:WRITE_SELF')")
    public ResponseEntity<ResponseDto<Void>> confirmTransaction(
            @PathVariable UUID transactionId,
            @Valid @RequestBody ConfirmTransactionRequest request) {

        confirmTransactionUseCase.confirm(transactionId, request.approve());

        String message = Boolean.TRUE.equals(request.approve())
                ? "Transaction approved"
                : "Transaction rejected";

        return ResponseEntity.ok(ResponseDto.success(null, message));
    }

    /**
     * Step 3 — Contributor confirms the product was handed to the transportation agency.
     * Completes the "delivered-to-transportation-confirmation" user task,
     * then the 30-second mock-delivery timer starts automatically.
     * Requires: TRANSACTION:WRITE_SELF (contributor role)
     */
    @PostMapping("/{transactionId}/shipped")
    @PreAuthorize("hasAuthority('TRANSACTION:WRITE_SELF')")
    public ResponseEntity<ResponseDto<Void>> confirmShipped(
            @PathVariable UUID transactionId) {

        confirmShippedUseCase.confirmShipped(transactionId);

        return ResponseEntity.ok(ResponseDto.success(null, "Product handed to transportation agency"));
    }

    /**
     * Step 4 — Buyer confirms delivery status.
     * Completes the "confirm-delivery-status" user task.
     * The status value drives the exclusive gateway:
     *   RECEIVED     → COMPLETED
     *   NOT_RECEIVED → retry delivery (up to 3 times)
     *   RETURNED     → return procedure activated
     * Requires: TRANSACTION:CREATE_SELF (buyer role)
     */
    @PostMapping("/{transactionId}/delivery")
    @PreAuthorize("hasAuthority('TRANSACTION:CREATE_SELF')")
    public ResponseEntity<ResponseDto<Void>> confirmDelivery(
            @PathVariable UUID transactionId,
            @Valid @RequestBody ConfirmDeliveryRequest request) {

        confirmDeliveryUseCase.confirmDelivery(transactionId, request.status());

        return ResponseEntity.ok(ResponseDto.success(null,
                "Delivery status recorded: " + request.status()));
    }

    /**
     * Step 5 — Contributor confirms whether the returned product was received back.
     * Completes the "ReturnConfirm" user task in the returning-products Camunda process.
     * Requires: TRANSACTION:WRITE_SELF (contributor role)
     */
        @PostMapping("/{transactionId}/confirm-return")
    @PreAuthorize("hasAuthority('TRANSACTION:WRITE_SELF')")
    public ResponseEntity<ResponseDto<Void>> confirmReturn(
            @PathVariable UUID transactionId,
            @Valid @RequestBody ConfirmReturnRequest request) {

        confirmReturnUseCase.confirmReturn(transactionId, request.received());

        String message = Boolean.TRUE.equals(request.received())
                ? "Returned product received — stock restored"
                : "Returned product reported as not received";

        return ResponseEntity.ok(ResponseDto.success(null, message));
    }
}

