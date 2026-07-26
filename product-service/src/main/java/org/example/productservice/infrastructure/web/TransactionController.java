package org.example.productservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.productservice.application.client.TokenGeneratorClient;
import org.example.productservice.application.command.CreateTransactionCommand;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateTransactionCommand;
import org.example.productservice.application.criteria.TransactionSearchCriteria;
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

    @PostMapping
    public ResponseEntity<ResponseDto<Void>> create(
            @Valid @RequestBody CreateTransactionRequest request,
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        UUID customerId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        CreateTransactionCommand command = transactionMapper.toCommand(request, customerId);
        transactionUseCase.create(command);
        return new ResponseEntity<>(ResponseDto.success(null, "Transaction created successfully"), HttpStatus.CREATED);
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<ResponseDto<TransactionResponse>> findById(@PathVariable UUID id) {
//        TransactionResponse data = transactionMapper.toResponse(transactionUseCase.findById(id));
//        return ResponseEntity.ok(ResponseDto.success(data));
//    }

    /**
     * User can only see their own transactions (where they are buyer or seller).
     * Example: GET /api/v1/transactions/search?status=PENDING&page=0&limit=20
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('TRANSACTION:READ_SELF')")
    public ResponseEntity<ResponseDto<List<TransactionResponse>>> search(
            @Valid @ModelAttribute TransactionFilter filter,
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        UUID userId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        TransactionSearchCriteria criteria = transactionMapper.toCriteria(filter, userId);
        PageCommand<Transaction> page = transactionUseCase.search(criteria);

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

    /**
     * Admin can see all transactions across all users.
     * Example: GET /api/v1/transactions/admin/search?productId=...&status=COMPLETED&page=0&limit=50
     */
    @GetMapping("/admin/search")
    @PreAuthorize("hasAuthority('TRANSACTION:READ_ALL')")
    public ResponseEntity<ResponseDto<List<TransactionResponse>>> adminSearch(
            @Valid @ModelAttribute TransactionFilter filter) {
        // userId = null → spec applies no user restriction
        TransactionSearchCriteria criteria = transactionMapper.toCriteria(filter, null);
        PageCommand<Transaction> page = transactionUseCase.search(criteria);

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
}
