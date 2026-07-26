package org.example.authservice.infrastructure.keycloak.httpclient;

import org.example.authservice.infrastructure.keycloak.dto.KeycloakCredentialRepresentation;
import org.example.authservice.infrastructure.keycloak.dto.KeycloakTokenResponse;
import org.example.authservice.infrastructure.keycloak.dto.KeycloakUserRepresentation;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange
public interface    KeycloakHttpClient {

    @PostExchange(value = "/realms/{realm}/protocol/openid-connect/logout", contentType = "application/x-www-form-urlencoded")
    void logout(
            @RequestBody MultiValueMap<String, String> form,
            @PathVariable String realm
    );

    @PostExchange(value = "/realms/{realm}/protocol/openid-connect/token", contentType = "application/x-www-form-urlencoded")
    KeycloakTokenResponse token(
            @PathVariable String realm,
            @RequestBody MultiValueMap<String, String> form
    );

    @PostExchange(value = "/admin/realms/{realm}/users", contentType = "application/json")
    void createUser(
            @PathVariable String realm,
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody KeycloakUserRepresentation user
    );

    @PutExchange(value = "/admin/realms/{realm}/users/{userId}", contentType = "application/json")
    void updateUser(
            @PathVariable String realm,
            @PathVariable String userId,
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody KeycloakUserRepresentation user
    );

    @PutExchange(value = "/admin/realms/{realm}/users/{userId}/reset-password", contentType = "application/json")
    void resetPassword(
            @PathVariable String realm,
            @PathVariable String userId,
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody KeycloakCredentialRepresentation credential
    );
}