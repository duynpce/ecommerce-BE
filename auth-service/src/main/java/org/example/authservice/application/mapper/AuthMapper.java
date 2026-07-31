package org.example.authservice.application.mapper;

import org.example.authservice.application.command.AuthTokenCommand;
import org.example.authservice.application.command.CompleteProfileCommand;
import org.example.authservice.application.command.CreateCredentialAccountCommand;
import org.example.authservice.application.command.RegisterCommand;
import org.example.authservice.domain.model.AccountCredential;
import org.example.authservice.domain.model.AuthToken;
import org.example.authservice.domain.model.Role;
import org.example.authservice.infrastructure.web.dto.CompleteProfileRequest;
import org.example.authservice.infrastructure.web.dto.CreateAccountRequest;
import org.example.authservice.infrastructure.web.dto.RegisterRequest;
import org.example.authservice.infrastructure.web.dto.TokenResponse;
import org.example.authservice.infrastructure.web.entity.AccountCredentialEntity;
import org.example.authservice.infrastructure.web.entity.AuthTokenEntity;
import org.example.authservice.infrastructure.web.entity.RoleEntity;

import java.util.UUID;

public interface AuthMapper {
    AuthTokenEntity toEntity(AuthToken domain);
    AuthToken toDomain(AuthTokenEntity domain);
    TokenResponse toDto(AuthTokenCommand authTokenCommand);

    RegisterCommand toCommand(RegisterRequest request);
    CreateAccountRequest toCreateAccountRequest(RegisterCommand command);
    CreateCredentialAccountCommand toCreateCredentialAccountCommand(RegisterCommand command);
    AccountCredentialEntity toEntity(AccountCredential domain);
    AccountCredential toDomain(CreateCredentialAccountCommand domain);
    AccountCredential toDomain(AccountCredentialEntity entity);

    Role toDomain(RoleEntity entity);

    CompleteProfileCommand toCommand(CompleteProfileRequest request, String email);
    CreateAccountRequest toCreateAccountRequest(CompleteProfileCommand command, UUID userId);
}
