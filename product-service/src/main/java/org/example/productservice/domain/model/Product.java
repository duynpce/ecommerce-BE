package org.example.productservice.domain.model;

import org.example.productservice.domain.constant.ProductCategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Product {
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

    public Product(UUID id, UUID shopId, UUID contributorId, List<String> imgUrls, String name,
                   String description, BigDecimal price, Integer quantity,
                   ProductCategory category, Map<String, String> attributes) {
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
    }

    public Product() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getShopId() {
        return shopId;
    }

    public void setShopId(UUID shopId) {
        this.shopId = shopId;
    }

    public UUID getContributorId() {
        return contributorId;
    }

    public void setContributorId(UUID contributorId) {
        this.contributorId = contributorId;
    }

    public List<String> getImgUrls() {
        return imgUrls;
    }

    public void setImgUrls(List<String> imgUrls) {
        this.imgUrls = imgUrls;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be null or negative");
        }
        this.quantity = quantity;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public BigDecimal calculateSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
