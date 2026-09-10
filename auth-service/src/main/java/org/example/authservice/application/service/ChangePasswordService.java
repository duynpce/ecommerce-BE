package org.example.authservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.authservice.application.client.KeycloakClient;
import org.example.authservice.application.command.ChangePasswordCommand;
import org.example.authservice.application.command.ResetKeycloakPasswordCommand;
import org.example.authservice.application.repository.AccountCredentialRepository;
import org.example.authservice.application.usecase.ChangePasswordUseCase;
import org.example.authservice.domain.exception.NotFoundException;
import org.example.authservice.domain.model.AccountCredential;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePasswordService implements ChangePasswordUseCase {

    private final AccountCredentialRepository accountCredentialRepository;
    private final PasswordEncoder             passwordEncoder;
    private final KeycloakClient              keycloakClient;

    @Override
    @Transactional
    public void changePassword(ChangePasswordCommand command) {
        // 1. Load the account (must be a local-auth account, i.e. has a username)
        AccountCredential account = accountCredentialRepository
                .findById(command.accountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        // 2. Domain-layer enforces: current password check + 48-h cooldown + regex
        account.changePassword(command.currentPassword(), command.newPassword(), passwordEncoder);

        // 3. Persist — Hibernate's @UpdateTimestamp will stamp updatedAt automatically
        accountCredentialRepository.save(account);

        // 4. Keep Keycloak in sync (only applies if a keycloakId is present)
        if (account.getKeycloakId() != null) {
            keycloakClient.resetPassword(
                    ResetKeycloakPasswordCommand.permanent(account.getKeycloakId(), command.newPassword())
            );
        }
    }
}
