package org.example.productservice.infrastructure.web.data.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.productservice.domain.constant.TransactionStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Top-level MongoDB document for a <em>transaction</em> — one checkout session
 * that may span multiple shops.
 *
 * <p>Relationship overview:
 * <pre>
 *   Transaction (1) ──── (*) SubOrder (1) ──── (*) OrderItemSnapshotEntity
 * </pre>
 * Product-level details live inside each {@link SubOrderEntity}.
 * This document stores only cross-shop aggregates, voucher info,
 * and the list of sub-order references.
 */
@Document(collection = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TransactionEntity extends BaseEntity {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();
    @Indexed
    private UUID customerId;
    @Builder.Default
    private List<UUID> subOrderIds = new ArrayList<>();
    private BigDecimal totalAmount;
    private UUID voucherId;
    private String voucherCode;
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private String description;
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;
}
