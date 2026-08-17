package org.example.productservice.application.criteria;

import org.example.productservice.domain.constant.SubOrderStatus;

import java.time.Instant;
import java.util.UUID;

public record SubOrderSearchCriteria(
        UUID customerId,
        UUID shopId,
        UUID contributorId,
        UUID transactionId,
        SubOrderStatus status,
        Instant createdFrom,
        Instant createdTo,
        int page,
        int limit
) {
    public SubOrderSearchCriteria {
        if (page < 0) page = 0;
        if (limit <= 0) limit = 20;
    }
}
