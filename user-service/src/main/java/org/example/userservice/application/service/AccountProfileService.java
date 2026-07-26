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
    public PageCommand<AccountProfile> getAccountReport(AccountProfileSearchCriteria criteria) {
        return accountProfileRepository.search(criteria);
    }
}