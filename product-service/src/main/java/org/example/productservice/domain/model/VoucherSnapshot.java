package org.example.productservice.domain.model;

import org.example.productservice.domain.constant.VoucherApplicableCategory;
import org.example.productservice.domain.constant.VoucherDiscountType;
import org.example.productservice.domain.constant.VoucherType;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VoucherSnapshot {
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
    private Set<VoucherApplicableCategory> applicableCategories = new HashSet<>();

    public VoucherSnapshot() {}

    public static VoucherSnapshot of(Voucher voucher, BigDecimal discountAmount) {
        VoucherSnapshot snapshot = new VoucherSnapshot();
        snapshot.voucherId = voucher.getId();
        snapshot.code = voucher.getCode();
        snapshot.name = voucher.getName();
        snapshot.type = voucher.getType();
        snapshot.shopId = voucher.getShopId();
        snapshot.discountType = voucher.getDiscountType();
        snapshot.discountValue = voucher.getDiscountValue();
        snapshot.minimumSpend = voucher.getMinimumSpend();
        snapshot.maximumDiscount = voucher.getMaximumDiscount();
        snapshot.discountAmount = discountAmount;
        snapshot.applicableCategories = new HashSet<>(voucher.getApplicableCategories());
        return snapshot;
    }

    public UUID getVoucherId() { return voucherId; }
    public void setVoucherId(UUID voucherId) { this.voucherId = voucherId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public VoucherType getType() { return type; }
    public void setType(VoucherType type) { this.type = type; }
    public UUID getShopId() { return shopId; }
    public void setShopId(UUID shopId) { this.shopId = shopId; }
    public VoucherDiscountType getDiscountType() { return discountType; }
    public void setDiscountType(VoucherDiscountType discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public BigDecimal getMinimumSpend() { return minimumSpend; }
    public void setMinimumSpend(BigDecimal minimumSpend) { this.minimumSpend = minimumSpend; }
    public BigDecimal getMaximumDiscount() { return maximumDiscount; }
    public void setMaximumDiscount(BigDecimal maximumDiscount) { this.maximumDiscount = maximumDiscount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public Set<VoucherApplicableCategory> getApplicableCategories() { return Set.copyOf(applicableCategories); }
    public void setApplicableCategories(Set<VoucherApplicableCategory> categories) {
        this.applicableCategories = categories == null ? new HashSet<>() : new HashSet<>(categories);
    }
}
