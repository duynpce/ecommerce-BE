package org.example.productservice.infrastructure.web.dto.voucher;

import java.math.BigDecimal;

public record VoucherApplicationResponse(
        VoucherSnapshotResponse voucher,
        BigDecimal transactionSubtotal,
        BigDecimal eligibleSubtotal,
        BigDecimal discountAmount
) {}
