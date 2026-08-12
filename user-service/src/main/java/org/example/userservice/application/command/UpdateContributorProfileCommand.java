package org.example.userservice.application.command;

public record UpdateContributorProfileCommand(
    String bankName,
    String bankAccountNumber,
    String taxId
) {}
