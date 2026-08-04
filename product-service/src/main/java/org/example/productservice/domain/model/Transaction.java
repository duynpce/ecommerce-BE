package org.example.productservice.domain.model;

import org.example.productservice.domain.constant.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Transaction {
    private UUID id;
    private UUID productId;
    private UUID contributorId;
    private UUID customerId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private TransactionStatus status;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public Transaction(UUID id, UUID productId, UUID contributorId, UUID customerId, Integer quantity, BigDecimal price, String description,
            Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.productId = productId;
        this.contributorId = contributorId;
        this.customerId = customerId;
        this.quantity = quantity;
        this.price = price;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.totalAmount = calculateTotal();
    }

    public Transaction() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getContributorId() {
        return contributorId;
    }

    public void setContributorId(UUID contributorId) {
        this.contributorId = contributorId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        if(totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0)
        {
            throw new IllegalArgumentException("Total amount must be a positive value");
        }

        this.totalAmount = totalAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public BigDecimal calculateTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
