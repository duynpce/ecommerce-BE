package org.example.productservice.application.command;

import java.util.UUID;

public record AddToCartCommand(
        UUID userId,
        UUID productId,
        int quantity
) {}
