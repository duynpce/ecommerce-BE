package org.example.userservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.userservice.application.command.CreateContributorProfileCommand;
import org.example.userservice.application.command.UpdateContributorProfileCommand;
import org.example.userservice.application.mapper.ContributorProfileMapper;
import org.example.userservice.application.repository.AccountProfileRepository;
import org.example.userservice.application.repository.ContributorProfileRepository;
import org.example.userservice.application.usecase.ContributorProfileUseCase;
import org.example.userservice.domain.exception.ConflictException;
import org.example.userservice.domain.exception.NotFoundException;
import org.example.userservice.domain.model.ContributorProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContributorProfileService implements ContributorProfileUseCase {

    private final ContributorProfileRepository contributorProfileRepository;
    private final AccountProfileRepository accountProfileRepository;
    private final ContributorProfileMapper contributorProfileMapper;

    @Override
    @Transactional
    public void createContributorProfile(CreateContributorProfileCommand command) {
        if (!accountProfileRepository.existsById(command.accountId())) {
            throw new NotFoundException("Account not found: " + command.accountId());
        }
        if (contributorProfileRepository.existsByAccountId(command.accountId())) {
            throw new ConflictException("Contributor profile already exists for account: " + command.accountId());
        }
        if (contributorProfileRepository.existsByIdentityCardNumber(command.identityCardNumber())) {
            throw new ConflictException("Identity card number already exists: " + command.identityCardNumber());
        }
        if (contributorProfileRepository.existsByBankAccountNumber(command.bankAccountNumber())) {
            throw new ConflictException("Bank account number already exists: " + command.bankAccountNumber());
        }
        if (contributorProfileRepository.existsByTaxId(command.taxId())) {
            throw new ConflictException("Tax ID already exists: " + command.taxId());
        }

        ContributorProfile contributorProfile = contributorProfileMapper.toDomain(command);
        contributorProfile.setAccountId(command.accountId());
        contributorProfileRepository.save(contributorProfile);
    }

    @Override
    public ContributorProfile getByAccountId(UUID accountId) {
        return contributorProfileRepository.findByAccountId(accountId);
    }

    @Override
    @Transactional
    public ContributorProfile updateContributorProfile(UUID accountId, UpdateContributorProfileCommand command) {
        ContributorProfile profile = contributorProfileRepository.findByAccountId(accountId);
        if (profile == null) {
            throw new NotFoundException("Contributor profile not found for account: " + accountId);
        }
        if (command.bankName() != null && !command.bankName().isBlank()) {
            profile.setBankName(command.bankName());
        }
        if (command.bankAccountNumber() != null && !command.bankAccountNumber().isBlank()) {
            String current = profile.getBankAccountNumber();
            if (!command.bankAccountNumber().equals(current)) {
                if (contributorProfileRepository.existsByBankAccountNumber(command.bankAccountNumber())) {
                    throw new ConflictException("Bank account number already exists: " + command.bankAccountNumber());
                }
                profile.setBankAccountNumber(command.bankAccountNumber());
            }
        }
        if (command.taxId() != null && !command.taxId().isBlank()) {
            profile.setTaxId(command.taxId());
        }
        contributorProfileRepository.save(profile);
        return profile;
    }

    @Override
    public boolean existsByAccountId(UUID accountId) {
        return contributorProfileRepository.existsByAccountId(accountId);
    }

    @Override
    public boolean existsByIdentityCardNumber(String identityCardNumber) {
        return contributorProfileRepository.existsByIdentityCardNumber(identityCardNumber);
    }

    @Override
    public boolean existsByBankAccountNumber(String bankAccountNumber) {
        return contributorProfileRepository.existsByBankAccountNumber(bankAccountNumber);
    }

    @Override
    public boolean existsByTaxId(String taxId) {
        return contributorProfileRepository.existsByTaxId(taxId);
    }
}
