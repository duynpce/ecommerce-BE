package org.example.authservice.infrastructure.web.data.adapter;

import lombok.RequiredArgsConstructor;
import org.example.authservice.application.mapper.AuthMapper;
import org.example.authservice.application.repository.AuthTokenRepository;
import org.example.authservice.domain.exception.UnauthorizedException;
import org.example.authservice.domain.model.AuthToken;
import org.example.authservice.infrastructure.prop.JwtProperties;
import org.example.authservice.infrastructure.web.data.springdata.SpringDataAuthTokenRepository;
import org.example.authservice.infrastructure.web.entity.AuthTokenEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthTokenRepositoryAdapter implements AuthTokenRepository {

    private final SpringDataAuthTokenRepository springDataRepo;
    private final AuthMapper mapper;
    private final JwtProperties jwtProperties;

    @Override
    public void save(AuthToken authToken) {
        AuthTokenEntity entity = new AuthTokenEntity();

        entity.setUserId(authToken.getUserId());
        entity.setAuthRefreshToken(authToken.getAuthRefreshToken());
        entity.setKeycloakRefreshToken(authToken.getKeycloakRefreshToken());
        entity.setSessionExpireAt(Instant.now().plus(jwtProperties.getRefreshExpirationDay(), ChronoUnit.DAYS));


        springDataRepo.save(entity);
    }

    @Override
    public AuthToken findByUserId(UUID userId) {
        return springDataRepo.findById(userId)
                .map(mapper::toDomain)
                .orElseThrow(() -> new UnauthorizedException("No active session found for user"));
    }

    @Override
    public void deleteByAuthRefreshToken(String authRefreshToken) {
        if (!springDataRepo.existsByAuthRefreshToken(authRefreshToken)) {
            throw new UnauthorizedException("Invalid or already logged out token");
        }
        springDataRepo.deleteByAuthRefreshToken(authRefreshToken);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        springDataRepo.deleteById(userId);
    }
}
