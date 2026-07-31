package org.example.userservice.application.usecase;

import org.example.userservice.application.command.CreateContributorProfileCommand;
import org.example.userservice.domain.model.ContributorProfile;

import java.util.UUID;

public interface ContributorProfileUseCase {

    void createContributorProfile(CreateContributorProfileCommand command);

    ContributorProfile getByAccountId(UUID accountId);

    boolean existsByAccountId(UUID accountId);

    boolean existsByIdentityCardNumber(String identityCardNumber);

    boolean existsByBankAccountNumber(String bankAccountNumber);

    boolean existsByTaxId(String taxId);
}
