package org.example.productservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.productservice.application.client.TokenGeneratorClient;
import org.example.productservice.application.command.CreateTransactionCommand;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateTransactionCommand;
import org.example.productservice.application.mapper.TransactionMapper;
import org.example.productservice.application.usecase.TransactionUseCase;
import org.example.productservice.domain.model.Transaction;
import org.example.productservice.infrastructure.web.dto.*;
import org.example.productservice.infrastructure.web.dto.transaction.CreateTransactionRequest;
import org.example.productservice.infrastructure.web.dto.transaction.TransactionFilter;
import org.example.productservice.infrastructure.web.dto.transaction.TransactionResponse;
import org.example.productservice.infrastructure.web.dto.transaction.UpdateTransactionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionUseCase transactionUseCase;
    private final TransactionMapper transactionMapper;
    private final TokenGeneratorClient tokenGeneratorClient;

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasAuthority('TRANSACTION:CREATE_SELF')")
    public ResponseEntity<ResponseDto<Void>> create(
            @Valid @RequestBody CreateTransactionRequest request,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UUID customerId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        CreateTransactionCommand command = transactionMapper.toCommand(request, customerId);
        transactionUseCase.create(command);

        return new ResponseEntity<>(ResponseDto.success(null, "Transaction created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<TransactionResponse>> findById(@PathVariable UUID id) {
        TransactionResponse data = transactionMapper.toResponse(transactionUseCase.findById(id));
        return ResponseEntity.ok(ResponseDto.success(data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto<TransactionResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request) {

        UpdateTransactionCommand command = transactionMapper.toCommand(request, id);
        TransactionResponse data = transactionMapper.toResponse(transactionUseCase.update(command));
        return ResponseEntity.ok(ResponseDto.success(data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        transactionUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    /**
     * Buyer sees only their own transactions.
     * Example: GET /api/v1/transactions/search?status=PENDING&page=0&limit=20
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('TRANSACTION:READ_SELF')")
    public ResponseEntity<ResponseDto<List<TransactionResponse>>> search(
            @Valid @ModelAttribute TransactionFilter filter,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UUID userId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        PageCommand<Transaction> page = transactionUseCase.search(transactionMapper.toCriteria(filter, userId));

        return toPageResponse(page, filter);
    }

    /**
     * Contributor sees only transactions where they are the seller.
     * Example: GET /api/v1/transactions/contributor/search?status=PENDING&page=0&limit=20
     */
    @GetMapping("/contributor/search")
    @PreAuthorize("hasAuthority('TRANSACTION:READ_SELF')")
    public ResponseEntity<ResponseDto<List<TransactionResponse>>> contributorSearch(
            @Valid @ModelAttribute TransactionFilter filter,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UUID contributorId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        PageCommand<Transaction> page = transactionUseCase.search(transactionMapper.toContributorCriteria(filter, contributorId));

        return toPageResponse(page, filter);
    }

    /**
     * Admin sees all transactions across all users.
     * Example: GET /api/v1/transactions/admin/search?productId=...&status=COMPLETED&page=0&limit=50
     */
    @GetMapping("/admin/search")
    @PreAuthorize("hasAuthority('TRANSACTION:READ_ALL')")
    public ResponseEntity<ResponseDto<List<TransactionResponse>>> adminSearch(
            @Valid @ModelAttribute TransactionFilter filter) {

        // userId = null → spec applies no user restriction
        PageCommand<Transaction> page = transactionUseCase.search(transactionMapper.toCriteria(filter, null));

        return toPageResponse(page, filter);
    }

    // -------------------------------------------------------------------------
    // State transitions — called by ticket-service Camunda delegates
    // -------------------------------------------------------------------------

    /**
     * Step 2a — Contributor approved the transaction.
     * Transitions: PENDING → PACKING
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ResponseDto<TransactionResponse>> approve(@PathVariable UUID id) {
        TransactionResponse data = transactionMapper.toResponse(transactionUseCase.approve(id));
        return ResponseEntity.ok(ResponseDto.success(data, "Transaction approved"));
    }

    /**
     * Step 2b — Contributor rejected the transaction.
     * Transitions: PENDING → REJECTED; restores product stock.
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ResponseDto<TransactionResponse>> reject(@PathVariable UUID id) {
        TransactionResponse data = transactionMapper.toResponse(transactionUseCase.reject(id));
        return ResponseEntity.ok(ResponseDto.success(data, "Transaction rejected and stock restored"));
    }

    /**
     * Step 3 — Contributor confirmed the product was handed to the carrier.
     * Transitions: PACKING → DELIVERED
     */
    @PatchMapping("/{id}/deliver")
    public ResponseEntity<ResponseDto<TransactionResponse>> markShipped(@PathVariable UUID id) {
        TransactionResponse data = transactionMapper.toResponse(transactionUseCase.markShipped(id));
        return ResponseEntity.ok(ResponseDto.success(data, "Transaction marked as shipped"));
    }

    /**
     * Step 4 — Buyer confirmed the product was received.
     * Transitions: DELIVERED → COMPLETED
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ResponseDto<TransactionResponse>> complete(@PathVariable UUID id) {
        TransactionResponse data = transactionMapper.toResponse(transactionUseCase.complete(id));
        return ResponseEntity.ok(ResponseDto.success(data, "Transaction completed"));
    }

    /**
     * Step 5 — Return process completed by contributor.
     * Transitions: DELIVERED → RETURNED; restores product stock.
     */
    @PostMapping("/{id}/return")
    public ResponseEntity<ResponseDto<TransactionResponse>> returnTransaction(@PathVariable UUID id) {
        TransactionResponse data = transactionMapper.toResponse(transactionUseCase.returnTransaction(id));
        return ResponseEntity.ok(ResponseDto.success(data, "Transaction marked as returned and stock restored"));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private ResponseEntity<ResponseDto<List<TransactionResponse>>> toPageResponse(
            PageCommand<Transaction> page, TransactionFilter filter) {

        List<TransactionResponse> data = page.getContent().stream()
                .map(transactionMapper::toResponse)
                .toList();

        MetaDto meta = MetaDto.builder()
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .paginationDto(new PaginationDto(filter.page(), filter.limit()))
                .build();

        return ResponseEntity.ok(ResponseDto.success(data, "Transactions fetched successfully", meta));
    }
}
