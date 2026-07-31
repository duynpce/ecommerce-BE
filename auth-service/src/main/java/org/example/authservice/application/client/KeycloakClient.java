// domain/client/KeycloakClient.java
package org.example.authservice.application.client;

import org.example.authservice.application.command.CreateKeycloakUserCommand;
import org.example.authservice.application.command.KeycloakTokenCommand;
import org.example.authservice.application.command.ResetKeycloakPasswordCommand;
import org.example.authservice.application.command.UpdateKeycloakUserCommand;

public interface KeycloakClient {
    KeycloakTokenCommand refresh(String keycloakRefreshToken, String email);
    void logout(String refreshToken);
    KeycloakTokenCommand exchangeCode(String code);
    void createUser(CreateKeycloakUserCommand command);
    void updateUser(UpdateKeycloakUserCommand command);
    void resetPassword(ResetKeycloakPasswordCommand command);
}