package org.example.productservice.infrastructure.web.data.entity;

import lombok.*;
import org.example.productservice.domain.constant.VoucherApplicableCategory;
import org.example.productservice.domain.constant.VoucherDiscountType;
import org.example.productservice.domain.constant.VoucherType;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherSnapshotEntity {
    private UUID voucherId;
    private String code;
    private String name;
    private VoucherType type;
    private UUID shopId;
    private VoucherDiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumSpend;
    private BigDecimal maximumDiscount;
    private BigDecimal discountAmount;
    @Builder.Default
    private Set<VoucherApplicableCategory> applicableCategories = new HashSet<>();
}
