package org.example.ticketservice.application.command;

import java.util.UUID;

public record CreateProductReviewCommand(
        UUID productId,
        UUID transactionId,
        UUID snapshotId,
        Integer rating,
        String comment
) {}
