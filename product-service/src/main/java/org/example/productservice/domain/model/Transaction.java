package org.example.productservice.domain.model;

import org.example.productservice.domain.constant.TransactionStatus;
import org.example.productservice.domain.constant.VoucherType;

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
    private BigDecimal subtotalAmount;
    private BigDecimal totalAmount;
    private List<VoucherSnapshot> vouchers = new ArrayList<>();
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private String phoneNumber;
    private String address;
    private String description;
    private TransactionStatus status;
    private String statusReason;
    private UUID triggerSubOrderId;

    // ── Constructors ───────────────────────────────────────────────────────────

    public Transaction() {}

    public Transaction(UUID id,
                       UUID customerId,
                       List<UUID> subOrderIds,
                       BigDecimal subtotalAmount,
                       BigDecimal totalAmount,
                       List<VoucherSnapshot> vouchers,
                       BigDecimal discountAmount,
                       String phoneNumber,
                       String address,
                       String description) {
        this.id             = id;
        this.customerId     = customerId;
        this.subOrderIds    = subOrderIds != null ? new ArrayList<>(subOrderIds) : new ArrayList<>();
        this.subtotalAmount = subtotalAmount;
        this.totalAmount    = totalAmount;
        this.vouchers       = vouchers != null ? new ArrayList<>(vouchers) : new ArrayList<>();
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        this.phoneNumber    = phoneNumber;
        this.address        = address;
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
        this.subtotalAmount = gross;
        BigDecimal requestedDiscount = vouchers.stream()
                .map(VoucherSnapshot::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.discountAmount = requestedDiscount.min(gross);
        this.totalAmount = gross.subtract(discountAmount);
    }

    public void applyVoucher(VoucherSnapshot voucher) {
        if (voucher == null || voucher.getDiscountAmount() == null
                || voucher.getDiscountAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount amount must be zero or positive");
        }
        if (vouchers.stream().anyMatch(existing -> existing.getType() == voucher.getType())) {
            throw new IllegalArgumentException("Only one " + voucher.getType() + " voucher can be applied");
        }
        this.vouchers.add(voucher);
        this.discountAmount = vouchers.stream()
                .map(VoucherSnapshot::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void applyVouchers(List<VoucherSnapshot> vouchers) {
        this.vouchers.clear();
        this.discountAmount = BigDecimal.ZERO;
        if (vouchers != null) vouchers.forEach(this::applyVoucher);
    }

    public void removeVoucher(VoucherType type) {
        this.vouchers.removeIf(voucher -> voucher.getType() == type);
        this.discountAmount = vouchers.stream()
                .map(VoucherSnapshot::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void removeVouchers() {
        this.vouchers.clear();
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

        if (totalAmount !=null && totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total amount cannot be negative");
        }
        this.totalAmount = totalAmount;
    }

    public BigDecimal getSubtotalAmount() { return subtotalAmount; }
    public void setSubtotalAmount(BigDecimal subtotalAmount) {
        if (subtotalAmount != null && subtotalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Subtotal amount cannot be negative");
        }
        this.subtotalAmount = subtotalAmount;
    }

    public List<VoucherSnapshot> getVouchers() { return Collections.unmodifiableList(vouchers); }
    public void setVouchers(List<VoucherSnapshot> vouchers) {
        this.vouchers = vouchers == null ? new ArrayList<>() : new ArrayList<>(vouchers);
    }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) {
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount amount must be zero or positive");
        }
        this.discountAmount = discountAmount;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getStatusReason() { return statusReason; }
    public void setStatusReason(String statusReason) { this.statusReason = statusReason; }

    public UUID getTriggerSubOrderId() { return triggerSubOrderId; }
    public void setTriggerSubOrderId(UUID triggerSubOrderId) { this.triggerSubOrderId = triggerSubOrderId; }
}
