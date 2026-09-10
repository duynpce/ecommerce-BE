package org.example.ticketservice.domain.model;

import java.time.Instant;
import java.util.UUID;

public record ReturnWorkItem(
        String taskId,
        UUID transactionId,
        UUID subOrderId,
        UUID snapshotId,
        UUID shopId,
        String productName,
        int retry,
        Instant createdAt
) {}
