package org.example.userservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.userservice.application.command.CreateProfileAccountCommand;
import org.example.userservice.application.command.PageCommand;
import org.example.userservice.application.criteria.AccountProfileSearchCriteria;
import org.example.userservice.application.mapper.AccountProfileMapper;
import org.example.userservice.application.repository.AccountProfileRepository;
import org.example.userservice.application.usecase.AccountProfileUseCase;
import org.example.userservice.domain.exception.ConflictException;
import org.example.userservice.domain.model.AccountProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.example.userservice.application.command.UpdateAccountProfileCommand;
import org.example.userservice.domain.exception.NotFoundException;
import org.example.userservice.domain.valueobject.PhoneNumber;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountProfileService implements AccountProfileUseCase {

    private final AccountProfileRepository accountProfileRepository;
    private final AccountProfileMapper accountProfileMapper;

    @Override
    @Transactional
    public void createAccount(CreateProfileAccountCommand command) {
        if (accountProfileRepository.existsByPhoneNumber(command.phoneNumber())) {
            throw new ConflictException("Phone number already exists: " + command.phoneNumber());
        }

        AccountProfile accountProfile = accountProfileMapper.toDomain(command);
        accountProfileRepository.save(accountProfile);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return accountProfileRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public AccountProfile getAccountProfileById(UUID id) {
        AccountProfile accountProfile = accountProfileRepository.findById(id);
        if (accountProfile == null) {
            throw new NotFoundException("Account profile not found with id: " + id);
        }
        return accountProfile;
    }

    @Override
    @Transactional
    public AccountProfile updateAccountProfile(UUID id, UpdateAccountProfileCommand command) {
        AccountProfile accountProfile = getAccountProfileById(id);

        if (command.phoneNumber() != null && !command.phoneNumber().isBlank()) {
            String currentPhone = accountProfile.getPhoneNumber() != null ? accountProfile.getPhoneNumber().getValue() : null;
            if (!command.phoneNumber().equals(currentPhone)) {
                if (accountProfileRepository.existsByPhoneNumber(command.phoneNumber())) {
                    throw new ConflictException("Phone number already exists: " + command.phoneNumber());
                }
                accountProfile.setPhoneNumber(new PhoneNumber(command.phoneNumber()));
            }
        }
        if (command.firstName() != null && !command.firstName().isBlank()) {
            accountProfile.setFirstName(command.firstName());
        }
        if (command.lastName() != null && !command.lastName().isBlank()) {
            accountProfile.setLastName(command.lastName());
        }
        if (command.address() != null && !command.address().isBlank()) {
            accountProfile.setAddress(command.address());
        }
        if (command.gender() != null) {
            accountProfile.setGender(command.gender());
        }

        accountProfileRepository.save(accountProfile);
        return accountProfile;
    }

    @Override
    public PageCommand<AccountProfile> getAccountReport(AccountProfileSearchCriteria criteria) {
        return accountProfileRepository.search(criteria);
    }
}