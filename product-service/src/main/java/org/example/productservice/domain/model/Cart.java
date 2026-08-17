package org.example.productservice.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain model representing a user's shopping cart.
 * A cart holds multiple {@link ProductSnapshot} line items — one per distinct
 * product. Adding the same product again merges quantities; removing reduces them.
 */
public class Cart extends BaseModel {

    private UUID id;
    private UUID userId;

    /** One snapshot per product line (same product = same entry, merged quantity). */
    private List<ProductSnapshot> items = new ArrayList<>();

    // ── Constructors ───────────────────────────────────────────────────────────

    public Cart() {}

    public Cart(UUID id, UUID userId) {
        this.id     = id;
        this.userId = userId;
    }

    // ── Business methods ───────────────────────────────────────────────────────

    /**
     * Adds a product snapshot to the cart.
     * If an item with the same {@code productId} already exists, the quantities
     * are merged instead of creating a duplicate line.
     */
    public void addItem(ProductSnapshot snapshot) {
        Optional<ProductSnapshot> existing = items.stream()
                .filter(i -> i.getProductId().equals(snapshot.getProductId()))
                .findFirst();

        if (existing.isPresent()) {
            ProductSnapshot item = existing.get();
            item.setQuantity(item.getQuantity() + snapshot.getQuantity());
        } else {
            items.add(snapshot);
        }
    }

    /**
     * Removes a product line from the cart by product ID.
     *
     * @return {@code true} if the item was present and removed, {@code false} otherwise
     */
    public boolean removeItem(UUID productId) {
        return items.removeIf(i -> i.getProductId().equals(productId));
    }

    /**
     * Removes multiple product lines from the cart by their product IDs.
     *
     * @return {@code true} if any item was present and removed, {@code false} otherwise
     */
    public boolean removeItems(List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return false;
        }
        return items.removeIf(i -> productIds.contains(i.getProductId()));
    }

    /**
     * Updates the quantity of an existing line item.
     * If {@code newQuantity} is 0 or less the item is removed entirely.
     *
     * @throws IllegalArgumentException if no line with {@code productId} exists
     */
    public void updateItemQuantity(UUID productId, int newQuantity) {
        if (newQuantity <= 0) {
            removeItem(productId);
            return;
        }
        items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found in cart: " + productId))
                .setQuantity(newQuantity);
    }

    /** Removes all items from the cart. */
    public void clear() {
        items.clear();
    }

    /** Calculates the grand total across all line items. */
    public BigDecimal calculateTotal() {
        return items.stream()
                .map(ProductSnapshot::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Returns {@code true} if the cart has no items. */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public List<ProductSnapshot> getItems() { return Collections.unmodifiableList(items); }
    public void setItems(List<ProductSnapshot> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }
}
