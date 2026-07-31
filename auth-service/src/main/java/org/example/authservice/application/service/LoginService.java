package org.example.authservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.authservice.application.client.TokenGeneratorClient;
import org.example.authservice.application.command.AuthTokenCommand;
import org.example.authservice.application.command.LoginCommand;
import org.example.authservice.application.repository.AccountCredentialRepository;
import org.example.authservice.application.repository.AuthTokenRepository;
import org.example.authservice.application.usecase.LoginUseCase;
import org.example.authservice.domain.constant.AccountStatus;
import org.example.authservice.domain.exception.UnauthorizedException;
import org.example.authservice.domain.model.AccountCredential;
import org.example.authservice.domain.model.AuthToken;
import org.example.authservice.infrastructure.prop.JwtProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase{
    private final AccountCredentialRepository accountCredentialRepository;
    private final TokenGeneratorClient tokenGeneratorClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final AuthTokenRepository authTokenRepository;

    @Override
    public AuthTokenCommand login(LoginCommand loginCommand) {
        AccountCredential accountCredential = accountCredentialRepository.findByUsername(loginCommand.username()).orElse(null);

        if(accountCredential == null || !passwordEncoder.matches(loginCommand.password(), accountCredential.getPassword())){
            throw new UnauthorizedException("invalid username or password");
        }

        if(accountCredential.getStatus() == AccountStatus.BLOCKED){
            throw new UnauthorizedException("Account is blocked. Please contact support.");
        }

        if(accountCredential.getStatus() == AccountStatus.CLOSED){
            throw new UnauthorizedException("Account is closed. Please contact support.");
        }

        return saveToken(accountCredential);
    }


    private AuthTokenCommand saveToken(AccountCredential accountCredential) {

        AuthTokenCommand newAuthToken = tokenGeneratorClient.
                generate(accountCredential.getUsername(), accountCredential.getId(),
                accountCredential.extractRoleNames(),accountCredential.extractPermissions());

        AuthToken updatedRecord = new AuthToken(
                accountCredential.getId(),
                newAuthToken.refreshToken(),
                null,
                Instant.now().plus(jwtProperties.getRefreshExpirationDay(), ChronoUnit.DAYS)
        );

        authTokenRepository.save(updatedRecord);
        return newAuthToken;
    }

}
