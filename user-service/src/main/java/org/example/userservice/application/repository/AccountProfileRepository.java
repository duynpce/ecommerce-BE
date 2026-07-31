package org.example.userservice.application.repository;

import org.example.userservice.application.command.PageCommand;
import org.example.userservice.application.criteria.AccountProfileSearchCriteria;
import org.example.userservice.domain.model.AccountProfile;

import java.util.UUID;

public interface AccountProfileRepository {
    void save(AccountProfile accountProfile);
    AccountProfile findById(UUID id);

    boolean existsById(UUID id);
    boolean existsByPhoneNumber(String phoneNumber);

    PageCommand<AccountProfile> search(AccountProfileSearchCriteria criteria);
}