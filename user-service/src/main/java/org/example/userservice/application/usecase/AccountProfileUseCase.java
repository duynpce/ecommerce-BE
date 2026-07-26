package org.example.userservice.application.usecase;

import org.example.userservice.application.command.CreateProfileAccountCommand;
import org.example.userservice.application.command.PageCommand;
import org.example.userservice.application.criteria.AccountProfileSearchCriteria;
import org.example.userservice.domain.model.AccountProfile;

public interface AccountProfileUseCase {
    void createAccount(CreateProfileAccountCommand command);

    boolean existsByPhoneNumber(String phoneNumber);

    PageCommand<AccountProfile> getAccountReport(AccountProfileSearchCriteria criteria);
}