package org.example.productservice.infrastructure.web.data.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.productservice.domain.constant.SubOrderStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Top-level MongoDB document for a <em>sub-order</em> — the per-shop slice
 * of a multi-shop checkout.
 *
 * <p>Relationship overview:
 * <pre>
 *   Transaction (1) ──── (*) SubOrder (1) ──── (*) OrderItemSnapshotEntity
 * </pre>
 * A {@link TransactionEntity} holds a list of {@code subOrderIds}.
 * Each {@code SubOrderEntity} belongs to exactly one shop and one transaction,
 * and embeds the product snapshots for that shop's portion of the order.
 */
@Document(collection = "sub_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SubOrderEntity extends BaseEntity {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    // ── References ─────────────────────────────────────────────────────────────

    /** Parent transaction that groups all sub-orders for one checkout. */
    @Indexed
    private UUID transactionId;

    /** The shop responsible for fulfilling this sub-order. */
    @Indexed
    private UUID shopId;

    /** The buyer who placed the order. */
    @Indexed
    private UUID customerId;

    // ── Line items ─────────────────────────────────────────────────────────────

    /**
     * Embedded snapshots of every product in this sub-order.
     * Data is frozen at checkout time so that subsequent product changes
     * do not affect historical records.
     */
    @Builder.Default
    private List<ProductSnapshotEntity> items = new ArrayList<>();

    // ── Financials ─────────────────────────────────────────────────────────────

    /**
     * Sum of {@code price × quantity} across all items, before shipping
     * or item-level discounts.
     */
    private BigDecimal subTotalAmount;

    /**
     * Shipping fee applied to this sub-order (shop-level, may be 0).
     */
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    /**
     * Final payable amount for this sub-order:
     * {@code subTotalAmount + shippingFee}.
     * Transaction-level voucher discounts are tracked on
     * {@link TransactionEntity#getDiscountAmount()} instead.
     */
    private BigDecimal totalAmount;

    // ── Meta ───────────────────────────────────────────────────────────────────

    /** Optional note from the buyer addressed to this specific shop. */
    private String note;

    /** Current fulfillment status of this sub-order. */
    @Builder.Default
    private SubOrderStatus status = SubOrderStatus.PENDING;
}
