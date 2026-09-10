package org.example.ticketservice.domain.model;

import java.time.Instant;
import java.util.UUID;

public record DeliveryWorkItem(
        String taskId,
        String stage,
        UUID transactionId,
        UUID subOrderId,
        UUID snapshotId,
        String productName,
        int retry,
        Instant createdAt
) {}
