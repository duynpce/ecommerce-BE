package org.example.productservice.domain.model;

import org.example.productservice.domain.constant.ProductCategory;
import org.example.productservice.domain.constant.VoucherApplicableCategory;
import org.example.productservice.domain.constant.VoucherDiscountType;
import org.example.productservice.domain.constant.VoucherType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Voucher extends BaseModel {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private VoucherType type;
    private UUID shopId;
    private VoucherDiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumSpend = BigDecimal.ZERO;
    private BigDecimal maximumDiscount;
    private Instant startsAt;
    private Instant endsAt;
    private Integer usageLimit;
    private Integer usedCount = 0;
    private Boolean active = true;
    private Set<VoucherApplicableCategory> applicableCategories = new HashSet<>(Set.of(VoucherApplicableCategory.ALL));

    public Voucher() {}

    public void validate() {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("Voucher code is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Voucher name is required");
        if (type == null) throw new IllegalArgumentException("Voucher type is required");
        if (type == VoucherType.SHOP && shopId == null) throw new IllegalArgumentException("Shop voucher requires a shopId");
        if (type != VoucherType.SHOP && shopId != null) throw new IllegalArgumentException(type + " voucher cannot have a shopId");
        if (discountType == null) throw new IllegalArgumentException("Discount type is required");
        if (discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount value must be positive");
        }
        if (discountType == VoucherDiscountType.PERCENTAGE && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Percentage discount cannot exceed 100");
        }
        if (minimumSpend == null || minimumSpend.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Minimum spend cannot be negative");
        }
        if (maximumDiscount != null && maximumDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Maximum discount must be positive when provided");
        }
        if (startsAt == null || endsAt == null || !endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("Voucher end time must be after its start time");
        }
        if (usageLimit != null && usageLimit <= 0) throw new IllegalArgumentException("Usage limit must be positive");
        if (applicableCategories == null || applicableCategories.isEmpty()) {
            throw new IllegalArgumentException("At least one applicable category is required");
        }
        if (applicableCategories.contains(VoucherApplicableCategory.ALL)) {
            applicableCategories = new HashSet<>(Set.of(VoucherApplicableCategory.ALL));
        }
    }

    public void requireAvailable(Instant now) {
        validate();
        if (!Boolean.TRUE.equals(active)) throw new IllegalStateException("Voucher is inactive");
        if (now.isBefore(startsAt)) throw new IllegalStateException("Voucher is not active yet");
        if (now.isAfter(endsAt)) throw new IllegalStateException("Voucher has expired");
        if (usageLimit != null && usedCount >= usageLimit) throw new IllegalStateException("Voucher usage limit has been reached");
    }

    public boolean appliesTo(ProductCategory category) {
        return applicableCategories.stream().anyMatch(item -> item.matches(category));
    }

    public BigDecimal calculateDiscount(BigDecimal eligibleSubtotal, BigDecimal transactionSubtotal) {
        if (transactionSubtotal == null || transactionSubtotal.compareTo(minimumSpend) < 0) {
            throw new IllegalStateException("Transaction subtotal before discount does not meet the voucher minimum spend");
        }
        if (eligibleSubtotal == null || eligibleSubtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Voucher does not apply to any selected product");
        }
        BigDecimal discount = discountType == VoucherDiscountType.PERCENTAGE
                ? eligibleSubtotal.multiply(discountValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : discountValue;
        if (maximumDiscount != null && discount.compareTo(maximumDiscount) > 0) discount = maximumDiscount;
        return discount.min(eligibleSubtotal).setScale(2, RoundingMode.HALF_UP);
    }

    public void incrementUsedCount() {
        this.usedCount = (usedCount == null ? 0 : usedCount) + 1;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code == null ? null : code.trim().toUpperCase(); }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public VoucherType getType() { return type; }
    public void setType(VoucherType type) { this.type = type; }
    public UUID getShopId() { return shopId; }
    public void setShopId(UUID shopId) { this.shopId = shopId; }
    public VoucherDiscountType getDiscountType() { return discountType; }
    public void setDiscountType(VoucherDiscountType discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public BigDecimal getMinimumSpend() { return minimumSpend; }
    public void setMinimumSpend(BigDecimal minimumSpend) { this.minimumSpend = minimumSpend == null ? BigDecimal.ZERO : minimumSpend; }
    public BigDecimal getMaximumDiscount() { return maximumDiscount; }
    public void setMaximumDiscount(BigDecimal maximumDiscount) { this.maximumDiscount = maximumDiscount; }
    public Instant getStartsAt() { return startsAt; }
    public void setStartsAt(Instant startsAt) { this.startsAt = startsAt; }
    public Instant getEndsAt() { return endsAt; }
    public void setEndsAt(Instant endsAt) { this.endsAt = endsAt; }
    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount == null ? 0 : usedCount; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active == null ? Boolean.TRUE : active; }
    public Set<VoucherApplicableCategory> getApplicableCategories() { return Set.copyOf(applicableCategories); }
    public void setApplicableCategories(Set<VoucherApplicableCategory> categories) {
        this.applicableCategories = categories == null ? new HashSet<>() : new HashSet<>(categories);
    }
}
