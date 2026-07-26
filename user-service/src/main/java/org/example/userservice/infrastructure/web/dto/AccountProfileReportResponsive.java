package org.example.userservice.infrastructure.web.dto;

import org.example.userservice.domain.constant.Gender;
import java.time.Instant;
import java.util.UUID;

public record AccountProfileReportResponsive(
    UUID id,
    String firstName,
    String lastName,
    String phoneNumber,
    String address,
    Gender gender,
    Instant createdAt,
    Instant updatedAt
) {}
