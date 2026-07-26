package org.example.userservice.application.repository;

import org.example.userservice.domain.model.ContributorProfile;

import java.util.UUID;

public interface ContributorProfileRepository {

    void save(ContributorProfile contributorProfile);

    ContributorProfile findByAccountId(UUID accountId);

    boolean existsByAccountId(UUID accountId);

    boolean existsByIdentityCardNumber(String identityCardNumber);

    boolean existsByBankAccountNumber(String bankAccountNumber);

    boolean existsByTaxId(String taxId);
}
