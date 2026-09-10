package org.example.userservice.application.usecase;

import org.example.userservice.application.command.CreateProfileAccountCommand;
import org.example.userservice.application.command.PageCommand;
import org.example.userservice.application.criteria.AccountProfileSearchCriteria;
import org.example.userservice.application.command.UpdateAccountProfileCommand;
import org.example.userservice.domain.model.AccountProfile;

import java.util.UUID;

public interface AccountProfileUseCase {
    void createAccount(CreateProfileAccountCommand command);

    boolean existsByPhoneNumber(String phoneNumber);

    AccountProfile getAccountProfileById(UUID id);

    AccountProfile updateAccountProfile(UUID id, UpdateAccountProfileCommand command);

    PageCommand<AccountProfile> getAccountReport(AccountProfileSearchCriteria criteria);
}