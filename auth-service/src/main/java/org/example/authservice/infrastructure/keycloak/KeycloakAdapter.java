package org.example.authservice.infrastructure.keycloak;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authservice.application.client.KeycloakClient;
import org.example.authservice.application.client.TokenGeneratorClient;
import org.example.authservice.application.command.CreateKeycloakUserCommand;
import org.example.authservice.application.command.KeycloakTokenCommand;
import org.example.authservice.application.command.ResetKeycloakPasswordCommand;
import org.example.authservice.application.command.UpdateKeycloakUserCommand;
import org.example.authservice.infrastructure.keycloak.dto.KeycloakCredentialRepresentation;
import org.example.authservice.infrastructure.keycloak.dto.KeycloakUserRepresentation;
import org.example.authservice.infrastructure.prop.KeycloakProperties;
import org.example.authservice.infrastructure.keycloak.httpclient.KeycloakHttpClient;
import org.example.authservice.infrastructure.keycloak.dto.KeycloakTokenResponse;
import org.example.authservice.application.mapper.KeycloakMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakAdapter implements KeycloakClient {

    private final KeycloakHttpClient keycloakHttpClient;
    private final KeycloakMapper keycloakMapper;
    private final KeycloakProperties props;
    private final TokenGeneratorClient tokenGeneratorClient;


    @Override
    public void logout(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id",     props.getClientId());
        form.add("client_secret", props.getClientSecret());
        form.add("refresh_token", refreshToken);

        keycloakHttpClient.logout(form, props.getRealm());
    }

    @Override
    public KeycloakTokenCommand refresh(String keycloakRefreshToken, String email) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type",    "refresh_token");
        form.add("client_id",     props.getClientId());
        form.add("client_secret", props.getClientSecret());
        form.add("refresh_token", keycloakRefreshToken);

        KeycloakTokenResponse response = keycloakHttpClient.token(props.getRealm(), form);
        UUID userId = tokenGeneratorClient.extractUserIdFromKeycloakAccessToken(response.getAccessToken());
        String refreshedEmail = tokenGeneratorClient.extractEmailFromKeycloakAccessToken(response.getAccessToken());

        return keycloakMapper.toKeycloakSession(response, refreshedEmail, userId);
    }

    @Override
    public KeycloakTokenCommand exchangeCode(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type",    "authorization_code");
        form.add("client_id",     props.getClientId());
        form.add("client_secret", props.getClientSecret());
        form.add("code",          code);
        form.add("redirect_uri",  props.getRedirectUri());

        KeycloakTokenResponse response = keycloakHttpClient.token(props.getRealm(), form);

        UUID userId = tokenGeneratorClient.extractUserIdFromKeycloakAccessToken(response.getAccessToken());
        String email = tokenGeneratorClient.extractEmailFromKeycloakAccessToken(response.getAccessToken());

        return keycloakMapper.toKeycloakSession(response, email, userId);
    }

    @Override
    public void createUser(CreateKeycloakUserCommand command) {
        String adminToken = loginAdmin();

        KeycloakUserRepresentation user = KeycloakUserRepresentation.builder()
                .username(command.username())
                .email(command.email())
                .enabled(true)
                .credentials(List.of(
                        KeycloakUserRepresentation.CredentialRepresentation.builder()
                                .value(command.password())
                                .temporary(false)
                                .build()
                ))
                .build();

        keycloakHttpClient.createUser(props.getRealm(), "Bearer " + adminToken, user);
    }

    @Override
    public void updateUser(UpdateKeycloakUserCommand command) {
        String adminToken = loginAdmin();

        KeycloakUserRepresentation user = KeycloakUserRepresentation.builder()
                .username(command.username())
                .email(null)
                .credentials(List.of(
                        KeycloakUserRepresentation.CredentialRepresentation.builder()
                                .value(command.password())
                                .temporary(false)
                                .build()
                ))
                .build();

        keycloakHttpClient.updateUser(
                props.getRealm(),
                command.keycloakUserId().toString(),
                "Bearer " + adminToken,
                user
        );
    }

    @Override
    public void resetPassword(ResetKeycloakPasswordCommand command) {
        String adminToken = loginAdmin();

        KeycloakCredentialRepresentation credential = KeycloakCredentialRepresentation.builder()
                .value(command.newPassword())
                .temporary(command.temporary())
                .build();

        keycloakHttpClient.resetPassword(
                props.getRealm(),
                command.keycloakUserId().toString(),
                "Bearer " + adminToken,
                credential
        );

        log.debug("Password reset via Keycloak Admin API for user: {}", command.keycloakUserId());
    }


    private String loginAdmin() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id",  "admin-cli");
        form.add("username",   props.getAdminUsername());
        form.add("password",   props.getAdminPassword());

        KeycloakTokenResponse response = keycloakHttpClient.token("master", form);
        log.debug("Admin login successful for realm: {}", props.getRealm());

        return response.getAccessToken();
    }
}