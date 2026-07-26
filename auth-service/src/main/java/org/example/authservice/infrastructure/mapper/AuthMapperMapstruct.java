package org.example.authservice.infrastructure.mapper;

import org.example.authservice.application.command.AuthTokenCommand;
import org.example.authservice.application.command.CompleteProfileCommand;
import org.example.authservice.application.command.CreateCredentialAccountCommand;
import org.example.authservice.application.command.RegisterCommand;
import org.example.authservice.application.mapper.AuthMapper;
import org.example.authservice.domain.model.AccountCredential;
import org.example.authservice.domain.model.AuthToken;
import org.example.authservice.domain.model.Permission;
import org.example.authservice.domain.model.Role;
import org.example.authservice.domain.valueobject.Email;
import org.example.authservice.infrastructure.web.dto.CompleteProfileRequest;
import org.example.authservice.infrastructure.web.dto.CreateAccountRequest;
import org.example.authservice.infrastructure.web.dto.RegisterRequest;
import org.example.authservice.infrastructure.web.dto.TokenResponse;
import org.example.authservice.infrastructure.web.entity.AccountCredentialEntity;
import org.example.authservice.infrastructure.web.entity.AuthTokenEntity;
import org.example.authservice.infrastructure.web.entity.PermissionEntity;
import org.example.authservice.infrastructure.web.entity.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface AuthMapperMapstruct extends AuthMapper {
    TokenResponse toDto(AuthTokenCommand authTokenCommand);
    AuthTokenEntity toEntity(AuthToken domain);
    AuthToken toDomain(AuthTokenEntity entity);

    RegisterCommand toCommand(RegisterRequest request);
    CreateAccountRequest toCreateAccountRequest(RegisterCommand command);
    CreateCredentialAccountCommand toCreateCredentialAccountCommand(RegisterCommand command);

    @Mapping(target = "email", source = "email", qualifiedByName = "emailToString")
    AccountCredentialEntity toEntity(AccountCredential domain);
    @Mapping(target = "email", source = "email", qualifiedByName = "stringToEmail")
    AccountCredential toDomain(CreateCredentialAccountCommand domain);
    @Mapping(target = "email", source = "email", qualifiedByName = "stringToEmail")
    AccountCredential toDomain(AccountCredentialEntity entity);


    Role toDomain(RoleEntity entity);

    Permission toDomain(PermissionEntity entity);

    CompleteProfileCommand toCommand(CompleteProfileRequest request, String email);

    @Override
    CreateAccountRequest toCreateAccountRequest(CompleteProfileCommand command, UUID userId);

    @Named("stringToEmail")
    default Email stringToEmail(String email) {
        return email == null ? null : new Email(email.toLowerCase());
    }

    @Named("emailToString")
    default String emailToString(Email email) {
        return email == null ? null : email.getValue().toLowerCase();
    }
}

