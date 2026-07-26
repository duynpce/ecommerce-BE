// infrastructure/keycloak/dto/KeycloakCredentialRepresentation.java
package org.example.authservice.infrastructure.keycloak.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KeycloakCredentialRepresentation {

    @Builder.Default
    private final String type = "password";
    private final String value;
    @Builder.Default
    @JsonProperty("temporary")
    private final boolean temporary = false;
}