package org.example.authservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.authservice.application.client.KeycloakClient;
import org.example.authservice.application.client.TokenGeneratorClient;
import org.example.authservice.application.client.UserClient;
import org.example.authservice.application.command.AuthTokenCommand;
import org.example.authservice.application.command.CompleteProfileCommand;
import org.example.authservice.application.command.ResetKeycloakPasswordCommand;
import org.example.authservice.application.command.UpdateKeycloakUserCommand;
import org.example.authservice.application.mapper.AuthMapper;
import org.example.authservice.application.repository.AccountCredentialRepository;
import org.example.authservice.application.usecase.CompleteProfileUseCase;
import org.example.authservice.domain.exception.ConflictException;
import org.example.authservice.domain.exception.NotFoundException;
import org.example.authservice.domain.model.AccountCredential;
import org.example.authservice.domain.constant.AccountStatus;
import org.example.authservice.infrastructure.web.dto.CreateAccountRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompleteProfileService implements CompleteProfileUseCase {

    private final AccountCredentialRepository accountCredentialRepository;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final KeycloakClient keycloakClient;
    private final UserClient userClient;
    private final TokenGeneratorClient tokenGeneratorClient;

    @Override
    @Transactional
    public AuthTokenCommand completeProfile(CompleteProfileCommand command) {

        // 1. Load the account that owns this PROFILE:COMPLETE:SELF token
        AccountCredential account = accountCredentialRepository.findByEmail(command.email())
                .orElseThrow(() -> new NotFoundException("Account not found for email: " + command.email()));

        // 2. Guard: this endpoint is only meaningful for INACTIVE accounts
        if (AccountStatus.ACTIVE.equals(account.getStatus())) {
            throw new ConflictException("Account is already active");
        }

        // 3. Username uniqueness check (same guard as RegisterService)
        if (accountCredentialRepository.existsByUsername(command.username())) {
            throw new ConflictException("Username already exists");
        }

        String email = account.getEmail().getValue();

        account.setUsername(command.username());
        account.setPassword(passwordEncoder.encode(command.password()));
        account.setStatus(AccountStatus.ACTIVE);
        accountCredentialRepository.save(account);

        CreateAccountRequest createAccountRequest = authMapper.toCreateAccountRequest(command, account.getId());
        userClient.createAccount(createAccountRequest);
        keycloakClient.updateUser(new UpdateKeycloakUserCommand(account.getKeycloakId(), command.username(), email, command.password()));
        keycloakClient.resetPassword(
                ResetKeycloakPasswordCommand.permanent(
                        account.getKeycloakId(),
                        command.password()
                )
        );

        return tokenGeneratorClient.generate(command.username(), account.getId(), account.extractRoleNames(), account.extractPermissions());
    }
}
