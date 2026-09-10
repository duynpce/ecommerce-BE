package org.example.userservice.application.mapper;

import org.example.userservice.application.command.CreateProfileAccountCommand;
import org.example.userservice.application.criteria.AccountProfileSearchCriteria;
import org.example.userservice.domain.model.AccountProfile;
import org.example.userservice.infrastructure.web.data.entity.AccountProfileEntity;
import org.example.userservice.infrastructure.web.dto.AccountProfileReportFilter;
import org.example.userservice.infrastructure.web.dto.AccountProfileReportResponsive;
import org.example.userservice.infrastructure.web.dto.CreateAccountProfileRequest;

import org.example.userservice.application.command.UpdateAccountProfileCommand;
import org.example.userservice.infrastructure.web.dto.AccountProfileResponse;
import org.example.userservice.infrastructure.web.dto.UpdateAccountProfileRequest;

public interface AccountProfileMapper {
    AccountProfile toDomain(AccountProfileEntity accountProfileEntity);
    AccountProfile toDomain(CreateProfileAccountCommand command);
    CreateProfileAccountCommand toCommand(CreateAccountProfileRequest request);
    AccountProfileEntity toEntity(AccountProfile accountProfile);
    AccountProfileReportResponsive toReportResponse(AccountProfile accountProfile);
    AccountProfileResponse toResponse(AccountProfile accountProfile);
    UpdateAccountProfileCommand toCommand(UpdateAccountProfileRequest request);
    AccountProfileSearchCriteria toCriteria(AccountProfileReportFilter filter);
}
