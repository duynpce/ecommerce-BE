package org.example.productservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.productservice.application.client.TokenGeneratorClient;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateSubOrderCommand;
import org.example.productservice.application.criteria.SubOrderSearchCriteria;
import org.example.productservice.application.mapper.SubOrderMapper;
import org.example.productservice.application.repository.ShopRepository;
import org.example.productservice.application.usecase.SubOrderUseCase;
import org.example.productservice.domain.constant.ProductSnapshotStatus;
import org.example.productservice.domain.exception.ForbiddenException;
import org.example.productservice.domain.exception.NotFoundException;
import org.example.productservice.domain.model.Shop;
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
    private final ShopRepository shopRepository;

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

    /**
     * Returns the ordered list of sub-order ID strings for a given transaction.
     * Called internally by the ticket-service Camunda delegate to populate the
     * multi-instance loop collection variable.
     */
    @GetMapping("/transaction/{transactionId}/ids")
    public ResponseEntity<ResponseDto<List<String>>> getSubOrderIds(@PathVariable UUID transactionId) {
        List<String> ids = subOrderUseCase.findByTransactionId(transactionId).stream()
                .map(so -> so.getId().toString())
                .toList();
        return ResponseEntity.ok(ResponseDto.success(ids));
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

    @PatchMapping("/{id}/items/{snapshotId}/isReviewed")
    @PreAuthorize("hasAuthority('TRANSACTION:WRITE_SELF')")
    public ResponseEntity<ResponseDto<Void>> markSnapshotIsReviewed(
            @PathVariable UUID id,
            @PathVariable UUID snapshotId,
            @RequestParam Boolean isReviewed) {

        subOrderUseCase.markSnapshotIsReviewed(id, snapshotId, isReviewed);

        return ResponseEntity.ok(ResponseDto.success(null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TRANSACTION:DELETE_SELF') or hasAuthority('TRANSACTION:DELETE_ALL')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        subOrderUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Camunda state-transition endpoints (called by ticket-service delegates)
    // -------------------------------------------------------------------------

    /**
     * Contributor approved the sub-order.
     * Transitions: PENDING → snapshots set to PACKING.
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ResponseDto<SubOrderResponse>> approve(@PathVariable UUID id) {
        SubOrderResponse data = subOrderMapper.toResponse(subOrderUseCase.approve(id));
        return ResponseEntity.ok(ResponseDto.success(data, "Sub-order approved"));
    }

    /**
     * Contributor rejected the sub-order.
     * Transitions: PENDING → REJECTED; product stock restored.
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ResponseDto<SubOrderResponse>> reject(@PathVariable UUID id) {
        SubOrderResponse data = subOrderMapper.toResponse(subOrderUseCase.reject(id));
        return ResponseEntity.ok(ResponseDto.success(data, "Sub-order rejected and stock restored"));
    }

    /**
     * User-initiated or timeout-triggered cancellation.
     * Transitions: any non-terminal state → CANCELLED; stock restored for un-fulfilled items.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ResponseDto<SubOrderResponse>> cancel(
            @PathVariable UUID id,
            @RequestBody(required = false) CancelSubOrderRequest request) {

        String reason = request != null ? request.reason() : null;
        SubOrderResponse data = subOrderMapper.toResponse(subOrderUseCase.cancel(id, reason));
        return ResponseEntity.ok(ResponseDto.success(data, "Sub-order cancelled and stock restored"));
    }

    /**
     * Contributor confirmed handoff to carrier for the complete sub-order.
     */
    @PatchMapping("/{id}/handoff")
    public ResponseEntity<ResponseDto<SubOrderResponse>> handoff(@PathVariable UUID id) {
        SubOrderResponse data = subOrderMapper.toResponse(subOrderUseCase.handoff(id));
        return ResponseEntity.ok(ResponseDto.success(data, "Sub-order handed to carrier"));
    }

    /** Completes carrier delivery for one snapshot and records deliveredAt. */
    @PatchMapping("/{id}/items/{snapshotId}/deliver")
    public ResponseEntity<ResponseDto<SubOrderResponse>> deliver(
            @PathVariable UUID id,
            @PathVariable UUID snapshotId) {

        SubOrderResponse data = subOrderMapper.toResponse(
                subOrderUseCase.deliver(id, snapshotId));
        return ResponseEntity.ok(ResponseDto.success(
                data, "Snapshot delivered and awaiting buyer confirmation"));
    }

    /**
     * Sub-order completion called by ticket-service completeSubOrderDelegate.
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ResponseDto<SubOrderResponse>> completeSubOrder(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSubOrderStatusRequest request) {

        SubOrderResponse data = subOrderMapper.toResponse(
                subOrderUseCase.completeSubOrder(id, request.status()));
        return ResponseEntity.ok(ResponseDto.success(data, "Sub-order terminal status updated"));
    }

    /**
     * Updates an individual item snapshot status directly.
     * Called internally by ticket-service delegates for snapshot-level sync/fallback.
     */
    @PatchMapping("/{id}/items/{snapshotId}/snapshot-status")
    public ResponseEntity<ResponseDto<SubOrderResponse>> updateSnapshotStatusInternal(
            @PathVariable UUID id,
            @PathVariable UUID snapshotId,
            @RequestParam ProductSnapshotStatus status) {

        SubOrder updated = subOrderUseCase.updateSnapshotStatus(id, snapshotId, status);
        return ResponseEntity.ok(ResponseDto.success(subOrderMapper.toResponse(updated), "Item snapshot status updated"));
    }

    /**
     * Backward-compatible snapshot route. Rejection always applies to the
     * complete sub-order and restores stock for all of its snapshots.
     */
    @PatchMapping("/{id}/items/{snapshotId}/reject")
    @PreAuthorize("hasAuthority('TRANSACTION:WRITE_SELF')")
    public ResponseEntity<ResponseDto<SubOrderResponse>> rejectSubOrderFromSnapshotRoute(
            @PathVariable UUID id,
            @PathVariable UUID snapshotId) {

        log.info("Rejecting whole sub-order from legacy snapshot route: subOrderId={}, snapshotId={}",
                id, snapshotId);
        SubOrder updated = subOrderUseCase.reject(id);
        return ResponseEntity.ok(ResponseDto.success(
                subOrderMapper.toResponse(updated), "Sub-order rejected and stock restored"));
    }

    /**
     * Backward-compatible snapshot route. Cancellation always applies to the
     * complete sub-order and restores stock for every active snapshot.
     */
    @PatchMapping("/{id}/items/{snapshotId}/cancel")
    @PreAuthorize("hasAuthority('TRANSACTION:WRITE_SELF') or hasAuthority('TRANSACTION:CREATE_SELF')")
    public ResponseEntity<ResponseDto<SubOrderResponse>> cancelSubOrderFromSnapshotRoute(
            @PathVariable UUID id,
            @PathVariable UUID snapshotId) {

        log.info("Cancelling whole sub-order from legacy snapshot route: subOrderId={}, snapshotId={}",
                id, snapshotId);
        SubOrder updated = subOrderUseCase.cancel(
                id, "Cancellation requested through item " + snapshotId);
        return ResponseEntity.ok(ResponseDto.success(
                subOrderMapper.toResponse(updated), "Sub-order cancelled and stock restored"));
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

    /**
     * Contributor sees sub-orders where they are the seller.
     * Example: GET /api/v1/sub-orders/contributor/search?status=PENDING&page=0&limit=20
     */
    @GetMapping("/contributor/search")
    @PreAuthorize("hasAuthority('TRANSACTION:READ_SELF')")
    public ResponseEntity<ResponseDto<List<SubOrderResponse>>> contributorSearch(
            @Valid @ModelAttribute SubOrderFilter filter,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UUID contributorId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        SubOrderSearchCriteria criteria = subOrderMapper.toContributorCriteria(filter, contributorId);
        PageCommand<SubOrder> page = subOrderUseCase.search(criteria);

        return toPageResponse(page, filter);
    }

    /**
     * Shop owner sees all sub-orders belonging to their shop.
     * Returns 403 if the authenticated user is not the owner of the shop.
     * Example: GET /api/v1/sub-orders/shop/{shopId}/search?status=PENDING&page=0&limit=20
     */
    @GetMapping("/shop/{shopId}/search")
    @PreAuthorize("hasAuthority('TRANSACTION:READ_SELF')")
    public ResponseEntity<ResponseDto<List<SubOrderResponse>>> shopSearch(
            @PathVariable UUID shopId,
            @Valid @ModelAttribute SubOrderFilter filter,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        UUID callerId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new NotFoundException("Shop not found: " + shopId));

        if (!callerId.equals(shop.getContributorId())) {
            throw new ForbiddenException("You are not the owner of this shop");
        }

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
