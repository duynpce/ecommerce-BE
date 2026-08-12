package org.example.productservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.productservice.application.client.TokenGeneratorClient;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateSubOrderCommand;
import org.example.productservice.application.criteria.SubOrderSearchCriteria;
import org.example.productservice.application.mapper.SubOrderMapper;
import org.example.productservice.application.usecase.SubOrderUseCase;
import org.example.productservice.domain.constant.ProductSnapshotStatus;
import org.example.productservice.domain.model.SubOrder;
import org.example.productservice.infrastructure.web.dto.MetaDto;
import org.example.productservice.infrastructure.web.dto.PaginationDto;
import org.example.productservice.infrastructure.web.dto.ResponseDto;
import org.example.productservice.infrastructure.web.dto.suborder.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sub-orders")
@RequiredArgsConstructor
@Slf4j
public class SubOrderController {

    private final SubOrderUseCase subOrderUseCase;
    private final SubOrderMapper subOrderMapper;
    private final TokenGeneratorClient tokenGeneratorClient;

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<SubOrderResponse>> findById(@PathVariable UUID id) {
        SubOrderResponse data = subOrderMapper.toResponse(subOrderUseCase.findById(id));
        return ResponseEntity.ok(ResponseDto.success(data));
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<ResponseDto<List<SubOrderResponse>>> findByTransactionId(@PathVariable UUID transactionId) {
        List<SubOrderResponse> list = subOrderUseCase.findByTransactionId(transactionId).stream()
                .map(subOrderMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ResponseDto.success(list));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<ResponseDto<List<SubOrderResponse>>> findByShopId(@PathVariable UUID shopId) {
        List<SubOrderResponse> list = subOrderUseCase.findByShopId(shopId).stream()
                .map(subOrderMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ResponseDto.success(list));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TRANSACTION:WRITE_SELF')")
    public ResponseEntity<ResponseDto<SubOrderResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSubOrderRequest request) {

        UpdateSubOrderCommand command = subOrderMapper.toCommand(request, id);
        SubOrderResponse data = subOrderMapper.toResponse(subOrderUseCase.update(command));
        return ResponseEntity.ok(ResponseDto.success(data));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('TRANSACTION:WRITE_SELF')")
    public ResponseEntity<ResponseDto<SubOrderResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSubOrderStatusRequest request) {

        SubOrder updated = subOrderUseCase.updateStatus(id, request.status());
        return ResponseEntity.ok(ResponseDto.success(subOrderMapper.toResponse(updated), "Sub-order status updated"));
    }

    @PatchMapping("/{id}/items/{snapshotId}/status")
    @PreAuthorize("hasAuthority('TRANSACTION:WRITE_SELF')")
    public ResponseEntity<ResponseDto<SubOrderResponse>> updateSnapshotStatus(
            @PathVariable UUID id,
            @PathVariable UUID snapshotId,
            @RequestParam ProductSnapshotStatus status) {

        SubOrder updated = subOrderUseCase.updateSnapshotStatus(id, snapshotId, status);
        return ResponseEntity.ok(ResponseDto.success(subOrderMapper.toResponse(updated), "Item snapshot status updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TRANSACTION:DELETE_SELF') or hasAuthority('TRANSACTION:DELETE_ALL')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        subOrderUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('TRANSACTION:READ_SELF')")
    public ResponseEntity<ResponseDto<List<SubOrderResponse>>> search(
            @Valid @ModelAttribute SubOrderFilter filter,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UUID customerId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        SubOrderSearchCriteria criteria = subOrderMapper.toCriteria(filter, customerId);
        PageCommand<SubOrder> page = subOrderUseCase.search(criteria);

        return toPageResponse(page, filter);
    }

    @GetMapping("/shop/{shopId}/search")
    @PreAuthorize("hasAuthority('TRANSACTION:READ_SELF')")
    public ResponseEntity<ResponseDto<List<SubOrderResponse>>> shopSearch(
            @PathVariable UUID shopId,
            @Valid @ModelAttribute SubOrderFilter filter) {

        SubOrderSearchCriteria criteria = subOrderMapper.toShopCriteria(filter, shopId);
        PageCommand<SubOrder> page = subOrderUseCase.search(criteria);

        return toPageResponse(page, filter);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private ResponseEntity<ResponseDto<List<SubOrderResponse>>> toPageResponse(
            PageCommand<SubOrder> page, SubOrderFilter filter) {

        List<SubOrderResponse> data = page.getContent().stream()
                .map(subOrderMapper::toResponse)
                .toList();

        MetaDto meta = MetaDto.builder()
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .paginationDto(new PaginationDto(filter.page(), filter.limit()))
                .build();

        return ResponseEntity.ok(ResponseDto.success(data, "Sub-orders fetched successfully", meta));
    }
}
