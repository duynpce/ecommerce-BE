package org.example.productservice.domain.model;

import org.example.productservice.domain.constant.ShopStatus;
import org.example.productservice.domain.valueobject.Address;

import java.util.UUID;

public class Shop extends BaseModel {
    private UUID id;
    private UUID contributorId;
    private String name;
    private String description;
    private String logoUrl;
    private Address pickUpAddress;
    private ShopStatus status;

    /** Cumulative average star rating across all shop products (0.0 – 5.0). */
    private Double rating;

    /** Total number of units sold across all products in this shop. */
    private Integer soldQuantity;

    private Integer oneStarRatingCount;
    private Integer twoStarRatingCount;
    private Integer threeStarRatingCount;
    private Integer fourStarRatingCount;
    private Integer fiveStarRatingCount;

    private Shop shop;

    public Shop() {
    }

    public Shop(UUID id, UUID contributorId, String name, String description,
                String logoUrl, Address pickUpAddress, ShopStatus status,
                Double rating
                ) {
        this.id = id;
        this.contributorId = contributorId;
        this.name = name;
        this.description = description;
        this.logoUrl = logoUrl;
        this.pickUpAddress = pickUpAddress;
        this.status = status;
        this.rating = 0.0;
        this.soldQuantity = 0;
        this.oneStarRatingCount = 0;
        this.twoStarRatingCount = 0;
        this.threeStarRatingCount = 0;
        this.fourStarRatingCount = 0;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getContributorId() { return contributorId; }
    public void setContributorId(UUID contributorId) { this.contributorId = contributorId; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Shop name cannot be null or empty");
        }
        this.name = name;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public Address getPickUpAddress() { return pickUpAddress; }
    public void setPickUpAddress(Address pickUpAddress) { this.pickUpAddress = pickUpAddress; }

    public ShopStatus getStatus() { return status; }
    public void setStatus(ShopStatus status) { this.status = status; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) {
        if(rating == null) {
            throw new IllegalArgumentException("Rating cannot be null");
        }

        if (rating != null && (rating < 0.0 || rating > 5.0)) {
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

    public void reCalculateRating(){
        int ratingCount = oneStarRatingCount + twoStarRatingCount + threeStarRatingCount + fourStarRatingCount + fiveStarRatingCount;

        setRating(ratingCount == 0 ? 0.0 :
                (double)(oneStarRatingCount + 2 * twoStarRatingCount + 3 * threeStarRatingCount + 4 * fourStarRatingCount + 5 * fiveStarRatingCount)
                / (double) ratingCount);
    }

    public void incrementSoldQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity to increment cannot be negative");
        }
        this.soldQuantity += quantity;
    }
}