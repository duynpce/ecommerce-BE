package org.example.userservice.application.client;

import java.util.Set;
import java.util.UUID;

public interface TokenGeneratorClient {

    UUID extractUserIdFromAccessToken(String accessToken);
    Set<String> extractAuthoritiesFromAccessToken(String accessToken);
}
