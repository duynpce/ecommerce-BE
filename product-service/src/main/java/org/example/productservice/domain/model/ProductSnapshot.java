package org.example.productservice.domain.model;

import org.example.productservice.domain.constant.ProductSnapshotStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable snapshot of a product captured at the moment of purchase / cart addition.
 * Storing a snapshot decouples orders and carts from live product data, so that
 * subsequent price or name changes on the product do not alter historical records.
 */
public class ProductSnapshot extends BaseModel {

    private UUID id;
    private UUID productId;
    private String name;
    private BigDecimal price;
    private ProductSnapshotStatus status;
    private Integer quantity;
    private String imageUrl;
    private Boolean isReviewed;
    private Instant deliveredAt;

    public ProductSnapshot() {}

    public ProductSnapshot(UUID id, UUID productId, String name, BigDecimal price, Integer quantity, String imageUrl) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Snapshot price cannot be null or negative");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Snapshot quantity must be positive");
        }
        this.id          = id;
        this.productId   = productId;
        this.name        = name;
        this.price       = price;
        this.quantity    = quantity;
        this.imageUrl    = imageUrl;
        this.isReviewed  = false;
        this.deliveredAt = null;
        this.status      = ProductSnapshotStatus.PENDING;
    }

    // ── Convenience factory ────────────────────────────────────────────────────

    public static ProductSnapshot of(Product product, int quantity) {
        String thumb = (product.getImgUrls() != null && !product.getImgUrls().isEmpty())
                ? product.getImgUrls().get(0)
                : null;
        return new ProductSnapshot(UUID.randomUUID(), product.getId(), product.getName(), product.getPrice(), quantity, thumb);
    }

    // ── Business helpers ───────────────────────────────────────────────────────

    /** Returns price × quantity for this line item. */
    public BigDecimal subtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Snapshot price cannot be null or negative");
        }
        this.price = price;
    }

    public ProductSnapshotStatus getStatus() { return status; }
    public void setStatus(ProductSnapshotStatus status) { this.status = status; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Snapshot quantity must be positive");
        }
        this.quantity = quantity;
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getIsReviewed() { return isReviewed; }
    public void setIsReviewed(Boolean isReviewed) { this.isReviewed = isReviewed; }

    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
}
