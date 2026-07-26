package org.example.userservice.infrastructure.web.data.adapter;

import lombok.RequiredArgsConstructor;
import org.example.userservice.application.command.CreateProfileAccountCommand;
import org.example.userservice.application.criteria.AccountProfileSearchCriteria;
import org.example.userservice.application.mapper.AccountProfileMapper;
import org.example.userservice.domain.model.AccountProfile;
import org.example.userservice.infrastructure.web.data.entity.AccountProfileEntity;
import org.example.userservice.infrastructure.mapper.AccountProfileMapperMapstruct;
import org.example.userservice.infrastructure.web.dto.AccountProfileReportFilter;
import org.example.userservice.infrastructure.web.dto.AccountProfileReportResponsive;
import org.example.userservice.infrastructure.web.dto.CreateAccountProfileRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountProfileProfileMapperAdapter implements AccountProfileMapper {
    private final AccountProfileMapperMapstruct accountProfileMapperMapstruct;

    @Override
    public AccountProfile toDomain(AccountProfileEntity accountProfileEntity) {
        return accountProfileMapperMapstruct.toDomain(accountProfileEntity);
    }

    @Override
    public AccountProfile toDomain(CreateProfileAccountCommand command) {
        return accountProfileMapperMapstruct.toDomain(command);
    }

    @Override
    public AccountProfileEntity toEntity(AccountProfile accountProfile) {
        return accountProfileMapperMapstruct.toEntity(accountProfile);
    }

    @Override
    public CreateProfileAccountCommand toCommand(CreateAccountProfileRequest request) {
        return accountProfileMapperMapstruct.toCommand(request);
    }

    @Override
    public AccountProfileReportResponsive toReportResponse(AccountProfile accountProfile) {
        return accountProfileMapperMapstruct.toReportResponse(accountProfile);
    }

    @Override
    public AccountProfileSearchCriteria toCriteria(AccountProfileReportFilter filter) {
        return accountProfileMapperMapstruct.toCriteria(filter);
    }
}
