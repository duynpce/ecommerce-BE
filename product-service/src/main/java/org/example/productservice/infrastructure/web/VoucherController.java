package org.example.productservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.productservice.application.client.TokenGeneratorClient;
import org.example.productservice.application.command.SaveVoucherCommand;
import org.example.productservice.application.command.VoucherLine;
import org.example.productservice.application.repository.ProductRepository;
import org.example.productservice.application.usecase.VoucherUseCase;
import org.example.productservice.domain.exception.NotFoundException;
import org.example.productservice.domain.model.Product;
import org.example.productservice.domain.model.Voucher;
import org.example.productservice.domain.model.VoucherApplication;
import org.example.productservice.domain.model.VoucherSnapshot;
import org.example.productservice.infrastructure.web.dto.ResponseDto;
import org.example.productservice.infrastructure.web.dto.transaction.CreateTransactionItemRequest;
import org.example.productservice.infrastructure.web.dto.voucher.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
public class VoucherController {
    private final VoucherUseCase voucherUseCase;
    private final ProductRepository productRepository;
    private final TokenGeneratorClient tokenGeneratorClient;

    @PostMapping
    @PreAuthorize("hasAuthority('VOUCHER:WRITE_ALL') or hasAuthority('VOUCHER:WRITE_SELF') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CONTRIBUTOR')")
    public ResponseEntity<ResponseDto<VoucherResponse>> create(
            @Valid @RequestBody SaveVoucherRequest request,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Authentication authentication) {
        Voucher saved = voucherUseCase.create(toCommand(request), actorId(accessToken), isAdmin(authentication));
        return new ResponseEntity<>(ResponseDto.success(toResponse(saved), "Voucher created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/manage")
    @PreAuthorize("hasAuthority('VOUCHER:READ_ALL') or hasAuthority('VOUCHER:READ_SELF') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CONTRIBUTOR')")
    public ResponseEntity<ResponseDto<List<VoucherResponse>>> manageable(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Authentication authentication) {
        List<VoucherResponse> data = voucherUseCase.findManageable(actorId(accessToken), isAdmin(authentication))
                .stream().map(this::toResponse).toList();
        return ResponseEntity.ok(ResponseDto.success(data));
    }

    @PostMapping("/available")
    @PreAuthorize("hasAuthority('TRANSACTION:CREATE_SELF')")
    public ResponseEntity<ResponseDto<List<VoucherApplicationResponse>>> available(
            @Valid @RequestBody VoucherAvailabilityRequest request) {
        List<VoucherApplicationResponse> data = voucherUseCase.findAvailable(toLines(request.items()))
                .stream().map(this::toApplicationResponse).toList();
        return ResponseEntity.ok(ResponseDto.success(data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto<VoucherResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseDto.success(toResponse(voucherUseCase.findById(id))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VOUCHER:WRITE_ALL') or hasAuthority('VOUCHER:WRITE_SELF') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CONTRIBUTOR')")
    public ResponseEntity<ResponseDto<VoucherResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody SaveVoucherRequest request,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Authentication authentication) {
        Voucher saved = voucherUseCase.update(id, toCommand(request), actorId(accessToken), isAdmin(authentication));
        return ResponseEntity.ok(ResponseDto.success(toResponse(saved), "Voucher updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VOUCHER:DELETE_ALL') or hasAuthority('VOUCHER:DELETE_SELF') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CONTRIBUTOR')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Authentication authentication) {
        voucherUseCase.delete(id, actorId(accessToken), isAdmin(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/preview")
    @PreAuthorize("hasAuthority('TRANSACTION:CREATE_SELF')")
    public ResponseEntity<ResponseDto<VoucherApplicationResponse>> preview(
            @Valid @RequestBody VoucherPreviewRequest request) {
        VoucherApplication application = voucherUseCase.preview(request.code(), toLines(request.items()));
        return ResponseEntity.ok(ResponseDto.success(toApplicationResponse(application)));
    }

    private List<VoucherLine> toLines(List<CreateTransactionItemRequest> items) {
        return items.stream().map(item -> {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + item.productId()));
            return new VoucherLine(product, item.quantity());
        }).toList();
    }

    private VoucherApplicationResponse toApplicationResponse(VoucherApplication application) {
        return new VoucherApplicationResponse(
                toSnapshotResponse(application.snapshot()),
                application.transactionSubtotal(),
                application.eligibleSubtotal(),
                application.discountAmount()
        );
    }

    private UUID actorId(String accessToken) {
        return tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_SUPER_ADMIN")
                        || authority.getAuthority().equals("VOUCHER:WRITE_ALL"));
    }

    private SaveVoucherCommand toCommand(SaveVoucherRequest request) {
        return new SaveVoucherCommand(request.code(), request.name(), request.description(), request.type(),
                request.shopId(), request.discountType(), request.discountValue(), request.minimumSpend(),
                request.maximumDiscount(), request.startsAt(), request.endsAt(), request.usageLimit(),
                request.active(), request.applicableCategories());
    }

    private VoucherResponse toResponse(Voucher voucher) {
        return new VoucherResponse(voucher.getId(), voucher.getCode(), voucher.getName(), voucher.getDescription(),
                voucher.getType(), voucher.getShopId(), voucher.getDiscountType(), voucher.getDiscountValue(),
                voucher.getMinimumSpend(), voucher.getMaximumDiscount(), voucher.getStartsAt(), voucher.getEndsAt(),
                voucher.getUsageLimit(), voucher.getUsedCount(), voucher.getActive(), voucher.getApplicableCategories(),
                voucher.getCreatedAt(), voucher.getUpdatedAt());
    }

    private VoucherSnapshotResponse toSnapshotResponse(VoucherSnapshot snapshot) {
        return new VoucherSnapshotResponse(snapshot.getVoucherId(), snapshot.getCode(), snapshot.getName(),
                snapshot.getType(), snapshot.getShopId(), snapshot.getDiscountType(), snapshot.getDiscountValue(),
                snapshot.getMinimumSpend(), snapshot.getMaximumDiscount(), snapshot.getDiscountAmount(),
                snapshot.getApplicableCategories());
    }
}
