package org.example.userservice.infrastructure.web.dto;

import java.time.Instant;
import java.util.UUID;

public record ContributorProfileResponse(
        UUID id,
        UUID accountId,
        String identityCardNumber,
        String bankName,
        String bankAccountNumber,
        String taxId,
        Instant createdAt,
        Instant updatedAt
) {}
