package org.example.productservice.domain.model;

import org.example.productservice.infrastructure.web.data.entity.BaseEntity;

import java.util.UUID;

public class ProductReview extends BaseModel {
    private UUID id;
    private UUID productId;
    private UUID userId;
    private UUID transactionId;
    private Integer rating;
    private String comment;

    private Product product;
    private Transaction transaction;

    // Constructors
    public ProductReview() {
    }

    public ProductReview(UUID id, UUID productId, UUID userId, UUID transactionId, Integer rating, String comment) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.transactionId = transactionId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
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
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }

        this.productId = productId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        this.userId = userId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction ID cannot be null");
        }

        this.transactionId = transactionId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        if(rating == null || rating > 5 || rating < 0) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }

        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
