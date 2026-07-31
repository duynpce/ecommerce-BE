package org.example.productservice.application.criteria;

import org.example.productservice.domain.constant.TransactionStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Filter fields for transaction search.
 * userId: when set (READ_SELF), the spec filters transactions where contributorId OR customerId matches.
 *         when null (READ_ALL), no user restriction is applied.
 */
public record TransactionSearchCriteria(
        UUID userId,
        UUID contributorId,
        UUID productId,
        TransactionStatus status,
        Instant createdFrom,
        Instant createdTo,
        Integer page,
        Integer limit
) {}
