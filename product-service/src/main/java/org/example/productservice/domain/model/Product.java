package org.example.productservice.domain.model;

import org.example.productservice.domain.constant.ProductCategory;
import org.example.productservice.domain.constant.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Product extends BaseModel {
    private UUID id;
    private UUID shopId;
    private UUID contributorId;
    private List<String> imgUrls;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private ProductCategory category;
    private Map<String, String> attributes;
    private ProductStatus status;

    /** Cumulative average star rating (0.0 – 5.0). */
    private Double rating;

    /** Total number of units sold. */
    private Integer soldQuantity;

    private Integer oneStarRatingCount;
    private Integer twoStarRatingCount;
    private Integer threeStarRatingCount;
    private Integer fourStarRatingCount;
    private Integer fiveStarRatingCount;

    private Shop shop;

    public Product() {
    }

    public Product(UUID id, UUID shopId, UUID contributorId, List<String> imgUrls, String name,
                   String description, BigDecimal price, Integer quantity,
                   ProductCategory category, Map<String, String> attributes
                   ) {
        this.id = id;
        this.shopId = shopId;
        this.contributorId = contributorId;
        this.imgUrls = imgUrls;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.attributes = attributes;
        this.status = ProductStatus.PENDING;
        this.rating = 0.0;
        this.soldQuantity = 0;
        this.oneStarRatingCount = 0;
        this.twoStarRatingCount = 0;
        this.threeStarRatingCount = 0;
        this.fourStarRatingCount = 0;
        this.fiveStarRatingCount = 0;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getShopId() { return shopId; }
    public void setShopId(UUID shopId) { this.shopId = shopId; }

    public UUID getContributorId() { return contributorId; }
    public void setContributorId(UUID contributorId) { this.contributorId = contributorId; }

    public List<String> getImgUrls() { return imgUrls; }
    public void setImgUrls(List<String> imgUrls) { this.imgUrls = imgUrls; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        this.name = name;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
        this.price = price;
    }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be null or negative");
        }
        this.quantity = quantity;
    }

    public ProductCategory getCategory() { return category; }
    public void setCategory(ProductCategory category) { this.category = category; }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public Map<String, String> getAttributes() { return attributes; }
    public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) {
        if(rating == null ){
            throw new IllegalArgumentException("Rating cannot be null ");
        }

        if ( (rating < 0.0 || rating > 5.0)) {
            throw new IllegalArgumentException("Rating must be between 0.0 and 5.0");
        }
        this.rating = rating;
    }

    public Integer getSoldQuantity() { return soldQuantity; }
    public void setSoldQuantity(Integer soldQuantity) {
        if (soldQuantity != null && soldQuantity < 0) {
            throw new IllegalArgumentException("Sold quantity cannot be negative");
        }
        this.soldQuantity = soldQuantity;
    }

    public Integer getOneStarRatingCount() { return oneStarRatingCount; }
    public void setOneStarRatingCount(Integer oneStarRatingCount) {
        if (oneStarRatingCount != null && oneStarRatingCount < 0) {
            throw new IllegalArgumentException("Star rating count cannot be negative");
        }
        this.oneStarRatingCount = oneStarRatingCount;
    }

    public Integer getTwoStarRatingCount() { return twoStarRatingCount; }
    public void setTwoStarRatingCount(Integer twoStarRatingCount) {
        if (twoStarRatingCount != null && twoStarRatingCount < 0) {
            throw new IllegalArgumentException("Star rating count cannot be negative");
        }
        this.twoStarRatingCount = twoStarRatingCount;
    }

    public Integer getThreeStarRatingCount() { return threeStarRatingCount; }
    public void setThreeStarRatingCount(Integer threeStarRatingCount) {
        if (threeStarRatingCount != null && threeStarRatingCount < 0) {
            throw new IllegalArgumentException("Star rating count cannot be negative");
        }
        this.threeStarRatingCount = threeStarRatingCount;
    }

    public Integer getFourStarRatingCount() { return fourStarRatingCount; }
    public void setFourStarRatingCount(Integer fourStarRatingCount) {
        if (fourStarRatingCount != null && fourStarRatingCount < 0) {
            throw new IllegalArgumentException("Star rating count cannot be negative");
        }
        this.fourStarRatingCount = fourStarRatingCount;
    }

    public Integer getFiveStarRatingCount() { return fiveStarRatingCount; }
    public void setFiveStarRatingCount(Integer fiveStarRatingCount) {
        if (fiveStarRatingCount != null && fiveStarRatingCount < 0) {
            throw new IllegalArgumentException("Star rating count cannot be negative");
        }
        this.fiveStarRatingCount = fiveStarRatingCount;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    // ── Business methods ───────────────────────────────────────────────────────

    public BigDecimal calculateSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    public void reCalculateRating(){
        int ratingCount = oneStarRatingCount + twoStarRatingCount + threeStarRatingCount + fourStarRatingCount + fiveStarRatingCount;

        setRating(ratingCount == 0 ? 0.0 :
                (double)(oneStarRatingCount + 2 * twoStarRatingCount + 3 * threeStarRatingCount + 4 * fourStarRatingCount + 5 * fiveStarRatingCount)
        / (double) ratingCount);
    }

    public void incrementSoldQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to increment cannot be negative");
        }
        this.soldQuantity += amount;
    }
}