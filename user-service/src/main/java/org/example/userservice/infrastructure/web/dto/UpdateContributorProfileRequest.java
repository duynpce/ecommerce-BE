package org.example.userservice.infrastructure.web.dto;

public record UpdateContributorProfileRequest(
    String bankName,
    String bankAccountNumber,
    String taxId
) {}
