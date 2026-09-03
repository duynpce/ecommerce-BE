package org.example.productservice.application.command;

import org.example.productservice.domain.constant.VoucherApplicableCategory;
import org.example.productservice.domain.constant.VoucherDiscountType;
import org.example.productservice.domain.constant.VoucherType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record SaveVoucherCommand(
        String code,
        String name,
        String description,
        VoucherType type,
        UUID shopId,
        VoucherDiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minimumSpend,
        BigDecimal maximumDiscount,
        Instant startsAt,
        Instant endsAt,
        Integer usageLimit,
        Boolean active,
        Set<VoucherApplicableCategory> applicableCategories
) {}
