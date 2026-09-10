package org.example.authservice.application.repository;

import org.example.authservice.domain.model.AuthToken;

import java.util.UUID;

public interface AuthTokenRepository {
    void save(AuthToken authToken);
    AuthToken findByUserId(UUID userId);
    void deleteByAuthRefreshToken(String authRefreshToken);
    void deleteByUserId(UUID userId);
}
