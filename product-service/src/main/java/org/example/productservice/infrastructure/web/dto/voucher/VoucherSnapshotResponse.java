package org.example.productservice.infrastructure.web.dto.voucher;

import org.example.productservice.domain.constant.VoucherApplicableCategory;
import org.example.productservice.domain.constant.VoucherDiscountType;
import org.example.productservice.domain.constant.VoucherType;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record VoucherSnapshotResponse(
        UUID voucherId,
        String code,
        String name,
        VoucherType type,
        UUID shopId,
        VoucherDiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minimumSpend,
        BigDecimal maximumDiscount,
        BigDecimal discountAmount,
        Set<VoucherApplicableCategory> applicableCategories
) {}
