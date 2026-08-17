package org.example.productservice.infrastructure.web.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequest(
        @NotNull(message = "Quantity cannot be null")
        @Min(value = 0, message = "Quantity must be 0 or greater (0 removes the item)")
        Integer quantity
) {}
