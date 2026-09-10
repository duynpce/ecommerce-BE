package org.example.productservice.infrastructure.web.data.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.productservice.domain.constant.VoucherApplicableCategory;
import org.example.productservice.domain.constant.VoucherDiscountType;
import org.example.productservice.domain.constant.VoucherType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Document(collection = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class VoucherEntity extends BaseEntity {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();
    @Indexed(unique = true)
    private String code;
    private String name;
    private String description;
    @Indexed
    private VoucherType type;
    @Indexed
    private UUID shopId;
    private VoucherDiscountType discountType;
    private BigDecimal discountValue;
    @Builder.Default
    private BigDecimal minimumSpend = BigDecimal.ZERO;
    private BigDecimal maximumDiscount;
    private Instant startsAt;
    private Instant endsAt;
    private Integer usageLimit;
    @Builder.Default
    private Integer usedCount = 0;
    @Builder.Default
    private Boolean active = true;
    @Builder.Default
    private Set<VoucherApplicableCategory> applicableCategories = new HashSet<>(Set.of(VoucherApplicableCategory.ALL));
}
