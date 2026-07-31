package org.example.userservice.application.command;

import java.util.UUID;

public record CreateContributorProfileCommand(
        UUID accountId,
        String identityCardNumber,
        String bankName,
        String bankAccountNumber,
        String taxId
) {
    public CreateContributorProfileCommand {
        if (accountId == null)           throw new IllegalArgumentException("Account ID cannot be null.");
        if (identityCardNumber == null)  throw new IllegalArgumentException("Identity card number cannot be null.");
        if (bankName == null)            throw new IllegalArgumentException("Bank name cannot be null.");
        if (bankAccountNumber == null)   throw new IllegalArgumentException("Bank account number cannot be null.");
        if (taxId == null)               throw new IllegalArgumentException("Tax ID cannot be null.");
    }
}
