package org.example.productservice.domain.model;

import java.math.BigDecimal;

public record VoucherApplication(
        Voucher voucher,
        BigDecimal transactionSubtotal,
        BigDecimal eligibleSubtotal,
        BigDecimal discountAmount,
        VoucherSnapshot snapshot
) {}
