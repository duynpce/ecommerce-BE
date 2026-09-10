package org.example.productservice.application.usecase;

import org.example.productservice.application.command.AddToCartCommand;
import org.example.productservice.application.command.UpdateCartItemCommand;
import org.example.productservice.domain.model.Cart;

import java.util.List;
import java.util.UUID;

public interface CartUseCase {

    /** Returns the cart for the given user (creates one if it does not exist yet). */
    Cart getCart(UUID userId);

    /** Adds a product to the cart, merging quantity if it already exists. */
    Cart addItem(AddToCartCommand command);

    /** Updates the quantity of a single line item (quantity = 0 removes the line). */
    Cart updateItem(UpdateCartItemCommand command);

    /** Removes product lines from the cart. */
    Cart removeItem(UUID userId, List<UUID> productIds);

    /** Removes all items from the cart. */
    void clearCart(UUID userId);
}
