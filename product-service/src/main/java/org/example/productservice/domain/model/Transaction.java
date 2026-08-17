package org.example.productservice.domain.model;

import org.example.productservice.domain.constant.TransactionStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Domain model for a <em>transaction</em> — one checkout session that may
 * span multiple shops.
 *
 * <p>Relationship overview:
 * <pre>
 *   Transaction (1) ──── (*) SubOrder (1) ──── (*) ProductSnapshot
 * </pre>
 * Product-level details and per-shop totals live inside each {@link SubOrder}.
 * This model owns the cross-shop aggregate total, voucher discount,
 * and the ordered list of sub-order references.
 */
public class Transaction extends BaseModel {

    private UUID id;
    private UUID customerId;
    private List<UUID> subOrderIds = new ArrayList<>();
    private BigDecimal totalAmount;
    private UUID voucherId;
    private String voucherCode;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private String description;
    private TransactionStatus status;

    // ── Constructors ───────────────────────────────────────────────────────────

    public Transaction() {}

    public Transaction(UUID id,
                       UUID customerId,
                       List<UUID> subOrderIds,
                       BigDecimal totalAmount,
                       UUID voucherId,
                       String voucherCode,
                       BigDecimal discountAmount,
                       String description) {
        this.id             = id;
        this.customerId     = customerId;
        this.subOrderIds    = subOrderIds != null ? new ArrayList<>(subOrderIds) : new ArrayList<>();
        this.totalAmount    = totalAmount;
        this.voucherId      = voucherId;
        this.voucherCode    = voucherCode;
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        this.description    = description;
        this.status         = TransactionStatus.PENDING;
    }

    // ── Business helpers ───────────────────────────────────────────────────────
    public void addSubOrderId(UUID subOrderId) {
        this.subOrderIds.add(subOrderId);
    }

    public void removeSubOrderId(UUID subOrderId) {
        this.subOrderIds.remove(subOrderId);
    }

    public void recalculateTotal(List<SubOrder> subOrders) {
        BigDecimal gross = subOrders.stream()
                .map(SubOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = gross.subtract(
                discountAmount != null ? discountAmount : BigDecimal.ZERO);
    }

    public void applyVoucher(UUID voucherId, String voucherCode, BigDecimal discountAmount) {
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount amount must be zero or positive");
        }
        this.voucherId      = voucherId;
        this.voucherCode    = voucherCode;
        this.discountAmount = discountAmount;
    }

    public void removeVoucher() {
        this.voucherId      = null;
        this.voucherCode    = null;
        this.discountAmount = BigDecimal.ZERO;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public List<UUID> getSubOrderIds() { return Collections.unmodifiableList(subOrderIds); }
    public void setSubOrderIds(List<UUID> subOrderIds) {
        this.subOrderIds = subOrderIds != null ? new ArrayList<>(subOrderIds) : new ArrayList<>();
    }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) {

        if (totalAmount !=null && totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be positive");
        }
        this.totalAmount = totalAmount;
    }

    public UUID getVoucherId() { return voucherId; }
    public void setVoucherId(UUID voucherId) { this.voucherId = voucherId; }

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) {
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount amount must be zero or positive");
        }
        this.discountAmount = discountAmount;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
}