package org.example.productservice.domain.model;

import org.example.productservice.domain.constant.ProductSnapshotStatus;
import org.example.productservice.domain.constant.SubOrderStatus;
import org.example.productservice.domain.exception.NotFoundException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Domain model for a <em>sub-order</em> — the per-shop slice of a multi-shop
 * checkout transaction.
 *
 * <p>Relationship:
 * <pre>
 *   Transaction (1) ──── (*) SubOrder (1) ──── (*) ProductSnapshot
 * </pre>
 * One {@link Transaction} references many {@code SubOrder}s (by ID).
 * Each {@code SubOrder} belongs to exactly one shop and embeds its own
 * list of {@link ProductSnapshot} line items.
 */
public class SubOrder extends BaseModel {

    private UUID id;

    /** Parent transaction that groups all sub-orders for one checkout. */
    private UUID transactionId;

    /** The shop responsible for fulfilling this sub-order. */
    private UUID shopId;

    /** The buyer. */
    private UUID customerId;

    private UUID contributorId;

    /** Frozen product snapshots for this shop's portion of the order. */
    private List<ProductSnapshot> items = new ArrayList<>();

    /** Sum of item subtotals before shipping. */
    private BigDecimal subTotalAmount;

    /** Shipping fee for this sub-order (may be 0). */
    private BigDecimal shippingFee;

    /**
     * Amount the buyer pays for this sub-order:
     * {@code subTotalAmount + shippingFee}.
     */
    private BigDecimal totalAmount;

    /** Optional buyer note to the shop. */
    private String note;

    private SubOrderStatus status;

    // ── Constructors ───────────────────────────────────────────────────────────

    public SubOrder() {
        this.status = SubOrderStatus.PENDING;
    }

    public SubOrder(UUID id,
                    UUID transactionId,
                    UUID shopId,
                    UUID customerId,
                    UUID contributorId,
                    List<ProductSnapshot> items,
                    BigDecimal shippingFee,
                    String note) {
        this.id            = id;
        this.transactionId = transactionId;
        this.shopId        = shopId;
        this.customerId    = customerId;
        this.contributorId = contributorId;
        this.items         = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.shippingFee   = shippingFee != null ? shippingFee : BigDecimal.ZERO;
        this.note          = note;
        this.status        = SubOrderStatus.PENDING;
        recalculate();
    }

    // ── Business helpers ───────────────────────────────────────────────────────

    /** Sums {@code price × quantity} for every line item. */
    public BigDecimal calculateSubTotal() {
        return items.stream()
                .map(ProductSnapshot::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Recomputes {@code subTotalAmount} and {@code totalAmount}
     * from current items and shipping fee.
     */
    public void recalculate() {
        this.subTotalAmount = calculateSubTotal();
        this.totalAmount    = subTotalAmount.add(
                shippingFee != null ? shippingFee : BigDecimal.ZERO);
    }

    /** Adds a line item and refreshes all totals. */
    public void addItem(ProductSnapshot snapshot) {
        this.items.add(snapshot);
        recalculate();
    }

    // ── Snapshot-level accessors & status management ──────────────────────────

    public Boolean getIsReviewedBySnapshotId(UUID snapshotId) {
        return findSnapshotOrThrow(snapshotId).getIsReviewed();
    }

    public void setIsReviewedBySnapshotId(UUID snapshotId, Boolean isReviewed) {
        findSnapshotOrThrow(snapshotId).setIsReviewed(isReviewed);
    }

    public Instant getSnapshotDeliveredAt(UUID snapshotId) {
        return findSnapshotOrThrow(snapshotId).getDeliveredAt();
    }

    public void setSnapshotDeliveredAt(UUID snapshotId, Instant deliveredAt) {
        findSnapshotOrThrow(snapshotId).setDeliveredAt(deliveredAt);
    }

    public void updateSnapshotStatus(UUID snapshotId, ProductSnapshotStatus newStatus) {
        ProductSnapshot snapshot = findSnapshotOrThrow(snapshotId);
        snapshot.setStatus(newStatus);
        if (newStatus == ProductSnapshotStatus.DELIVERED_AWAITING_CONFIRMATION
                || newStatus == ProductSnapshotStatus.RECEIVED
                || newStatus == ProductSnapshotStatus.COMPLETED) {
            if (snapshot.getDeliveredAt() == null) {
                snapshot.setDeliveredAt(Instant.now());
            }
        }
        recalculateStatus();
    }

    public void recalculateStatus() {
        if (items == null || items.isEmpty()) return;

        boolean allTerminal = items.stream().allMatch(i ->
                i.getStatus() == ProductSnapshotStatus.COMPLETED ||
                i.getStatus() == ProductSnapshotStatus.RETURNED ||
                i.getStatus() == ProductSnapshotStatus.CANCELLED ||
                i.getStatus() == ProductSnapshotStatus.REJECTED
        );

        if (allTerminal) {
            boolean allCompleted = items.stream().allMatch(i -> i.getStatus() == ProductSnapshotStatus.COMPLETED);
            boolean allReturned  = items.stream().allMatch(i -> i.getStatus() == ProductSnapshotStatus.RETURNED);
            boolean allCancelled = items.stream().allMatch(i -> i.getStatus() == ProductSnapshotStatus.CANCELLED);
            boolean allRejected  = items.stream().allMatch(i -> i.getStatus() == ProductSnapshotStatus.REJECTED);

            if (allCompleted) {
                this.status = SubOrderStatus.COMPLETED;
            } else if (allReturned) {
                this.status = SubOrderStatus.RETURNED;
            } else if (allCancelled) {
                this.status = SubOrderStatus.CANCELLED;
            } else if (allRejected) {
                this.status = SubOrderStatus.REJECTED;
            } else {
                this.status = SubOrderStatus.PARTIALLY_RETURNED;
            }
        }
    }

    private ProductSnapshot findSnapshotOrThrow(UUID snapshotId) {
        return items.stream()
                .filter(item -> item.getId().equals(snapshotId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Product snapshot not found for ID: " + snapshotId));
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }

    public UUID getShopId() { return shopId; }
    public void setShopId(UUID shopId) { this.shopId = shopId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getContributorId() { return contributorId; }
    public void setContributorId(UUID contributorId) { this.contributorId = contributorId; }

    public List<ProductSnapshot> getItems() { return Collections.unmodifiableList(items); }
    public void setItems(List<ProductSnapshot> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        recalculate();
    }

    /** Read-only — derived from items. Call {@link #recalculate()} to refresh. */
    public BigDecimal getSubTotalAmount() { return subTotalAmount; }

    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee != null ? shippingFee : BigDecimal.ZERO;
        recalculate();
    }

    /** Read-only — derived from subTotalAmount + shippingFee. */
    public BigDecimal getTotalAmount() { return totalAmount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public SubOrderStatus getStatus() { return status; }
    public void setStatus(SubOrderStatus status) { this.status = status; }
}
