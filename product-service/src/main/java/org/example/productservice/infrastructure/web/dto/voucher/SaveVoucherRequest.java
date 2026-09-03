package org.example.productservice.infrastructure.web.dto.voucher;

import jakarta.validation.constraints.*;
import org.example.productservice.domain.constant.VoucherApplicableCategory;
import org.example.productservice.domain.constant.VoucherDiscountType;
import org.example.productservice.domain.constant.VoucherType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record SaveVoucherRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        @NotNull VoucherType type,
        UUID shopId,
        @NotNull VoucherDiscountType discountType,
        @NotNull @DecimalMin(value = "0.01") BigDecimal discountValue,
        @NotNull @DecimalMin(value = "0") BigDecimal minimumSpend,
        @DecimalMin(value = "0.01") BigDecimal maximumDiscount,
        @NotNull Instant startsAt,
        @NotNull Instant endsAt,
        @Min(1) Integer usageLimit,
        @NotNull Boolean active,
        @NotEmpty Set<VoucherApplicableCategory> applicableCategories
) {}
