package org.example.userservice.infrastructure.mapper;

import org.example.userservice.application.command.CreateProfileAccountCommand;
import org.example.userservice.application.criteria.AccountProfileSearchCriteria;
import org.example.userservice.domain.model.AccountProfile;
import org.example.userservice.domain.valueobject.PhoneNumber;
import org.example.userservice.infrastructure.web.dto.AccountProfileReportFilter;
import org.example.userservice.infrastructure.web.data.entity.AccountProfileEntity;
import org.example.userservice.infrastructure.web.dto.AccountProfileReportResponsive;
import org.example.userservice.infrastructure.web.dto.CreateAccountProfileRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.example.userservice.application.command.UpdateAccountProfileCommand;
import org.example.userservice.infrastructure.web.dto.AccountProfileResponse;
import org.example.userservice.infrastructure.web.dto.UpdateAccountProfileRequest;

@Mapper(componentModel = "spring")
public interface AccountProfileMapperMapstruct {

    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "stringToPhoneNumber")
    AccountProfile toDomain(AccountProfileEntity accountProfileEntity);

    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "stringToPhoneNumber")
    AccountProfile toDomain(CreateProfileAccountCommand command);

    @Mapping(target = "id", source = "userId")
    CreateProfileAccountCommand toCommand(CreateAccountProfileRequest request);

    UpdateAccountProfileCommand toCommand(UpdateAccountProfileRequest request);

    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "phoneNumberToString")
    AccountProfileEntity toEntity(AccountProfile accountProfile);

    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "phoneNumberToString")
    AccountProfileReportResponsive toReportResponse(AccountProfile accountProfile);

    @Mapping(target = "phoneNumber", source = "phoneNumber", qualifiedByName = "phoneNumberToString")
    AccountProfileResponse toResponse(AccountProfile accountProfile);

    @Mapping(target = "createdFrom", source = "createdFrom", qualifiedByName = "localDateToInstantStartOfDay")
    @Mapping(target = "createdTo", source = "createdTo", qualifiedByName = "localDateToInstantEndOfDay")
    AccountProfileSearchCriteria toCriteria(AccountProfileReportFilter filter);

    @Named("phoneNumberToString")
    default String phoneNumberToString(PhoneNumber phoneNumber) {
        return phoneNumber != null ? phoneNumber.getValue() : null;
    }

    @Named("stringToPhoneNumber")
    default PhoneNumber stringToPhoneNumber(String phoneNumberRaw) {
        return phoneNumberRaw != null ? new PhoneNumber(phoneNumberRaw) : null;
    }

    @Named("localDateToInstantStartOfDay")
    default Instant localDateToInstantStartOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
    }

    @Named("localDateToInstantEndOfDay")
    default Instant localDateToInstantEndOfDay(LocalDate date) {
        return date != null ? date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusMillis(1) : null;
    }
}
