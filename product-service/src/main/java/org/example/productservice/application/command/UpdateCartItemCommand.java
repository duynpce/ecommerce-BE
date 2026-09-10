package org.example.productservice.application.command;

import java.util.UUID;

public record UpdateCartItemCommand(
        UUID userId,
        UUID productId,
        int quantity
) {}
