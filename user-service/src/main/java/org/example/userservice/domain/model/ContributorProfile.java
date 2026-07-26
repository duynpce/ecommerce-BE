package org.example.userservice.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class ContributorProfile {

    private UUID accountId;
    private String identityCardNumber;
    private String bankName;
    private String bankAccountNumber;
    private String taxId;
    private Instant createdAt;
    private Instant updatedAt;

    public ContributorProfile(UUID accountId,
                              String identityCardNumber,
                              String bankName,
                              String bankAccountNumber,
                              String taxId) {
        this.accountId           = Objects.requireNonNull(accountId,           "Account ID cannot be null");
        this.identityCardNumber  = Objects.requireNonNull(identityCardNumber,  "Identity card number cannot be null");
        this.bankName            = Objects.requireNonNull(bankName,            "Bank name cannot be null");
        this.bankAccountNumber   = Objects.requireNonNull(bankAccountNumber,   "Bank account number cannot be null");
        this.taxId               = Objects.requireNonNull(taxId,               "Tax ID cannot be null");
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) { this.accountId = accountId; }

    public String getIdentityCardNumber() { return identityCardNumber; }
    public void setIdentityCardNumber(String identityCardNumber) { this.identityCardNumber = identityCardNumber; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}