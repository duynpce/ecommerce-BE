package org.example.authservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authservice.application.client.KeycloakClient;
import org.example.authservice.application.client.TokenGeneratorClient;
import org.example.authservice.application.command.AuthTokenCommand;
import org.example.authservice.application.command.CallbackCommand;
import org.example.authservice.application.command.KeycloakTokenCommand;
import org.example.authservice.application.repository.AccountCredentialRepository;
import org.example.authservice.application.repository.AuthTokenRepository;
import org.example.authservice.application.repository.RoleRepository;
import org.example.authservice.application.usecase.CallbackUseCase;
import org.example.authservice.domain.constant.AccountStatus;
import org.example.authservice.domain.exception.NotFoundException;
import org.example.authservice.domain.exception.UnauthorizedException;
import org.example.authservice.domain.model.AccountCredential;
import org.example.authservice.domain.model.AuthToken;
import org.example.authservice.domain.valueobject.Email;
import org.example.authservice.infrastructure.prop.JwtProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallbackService implements CallbackUseCase {

    private final KeycloakClient keycloakClient;
    private final TokenGeneratorClient tokenGeneratorClient;
    private final AuthTokenRepository authTokenRepository;
    private final AccountCredentialRepository accountCredentialRepository;
    private final JwtProperties jwtProperties;
    private final RoleRepository roleRepository;


    @Override
    @Transactional
    public AuthTokenCommand remoteCallback(CallbackCommand command) {
        log.info("Remote OAuth2 callback received, exchanging authorization code for tokens");

        KeycloakTokenCommand keycloakSession = keycloakClient.exchangeCode(command.code());
        UUID keycloakUserId = keycloakSession.userId();
        String email = keycloakSession.email();

        Optional<AccountCredential> accountCredentialOptional = accountCredentialRepository.findByEmail(email);
        AccountCredential accountCredential = accountCredentialOptional.orElse(null);

        //if there is no account with email --> sync it in our db
        if(accountCredentialOptional.isEmpty()) {
            accountCredential = new AccountCredential();
            accountCredential.setEmail(new Email(email));
            accountCredential.setKeycloakId(keycloakUserId);
            accountCredential.setStatus(AccountStatus.INACTIVE);
            accountCredential.setRoles(Set.of(roleRepository.getDefaultRole().orElseThrow(() -> new NotFoundException("Default role not found"))));
            accountCredential = accountCredentialRepository.save(accountCredential);
        }
        //if keycloak id not exist then save it
        else{
            if(accountCredential.getKeycloakId() == null){
                accountCredential.setKeycloakId(keycloakUserId);
                accountCredential = accountCredentialRepository.save(accountCredential);
            }

        }

        if(accountCredential.getStatus() == AccountStatus.BLOCKED){
            throw new UnauthorizedException("Account is blocked. Please contact support.");
        }

        if(accountCredential.getStatus() == AccountStatus.CLOSED){
            throw new UnauthorizedException("Account is closed. Please contact support.");
        }


        Set<String> roles = accountCredential.extractRoleNames();
        Set<String> permissions = accountCredential.getStatus() == AccountStatus.ACTIVE ? accountCredential.extractPermissions() : Set.of("PROFILE:COMPLETE_SELF");

        return buildAndSaveAuthToken(keycloakSession.email(), accountCredential.getId(), keycloakSession.refreshToken(), roles, permissions);
    }


    private AuthTokenCommand buildAndSaveAuthToken(String email, UUID userId,
                                                    String keycloakRefreshToken,
                                                    Set<String> roles,
                                                    Set<String> permissions) {
        AuthTokenCommand authToken = tokenGeneratorClient.generate(email, userId, roles, permissions);

        AuthToken authTokenRecord = new AuthToken(
                userId,
                authToken.refreshToken(),
                keycloakRefreshToken,
                Instant.now().plus(jwtProperties.getRefreshExpirationDay(), ChronoUnit.DAYS)
        );

        authTokenRepository.save(authTokenRecord);
        log.info("Callback successful for userId: {}", userId);
        return authToken;
    }
}
