package org.example.authservice.infrastructure.token;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.example.authservice.application.client.TokenGeneratorClient;
import org.example.authservice.application.command.AuthTokenCommand;
import org.example.authservice.domain.exception.UnauthorizedException;
import org.example.authservice.infrastructure.prop.JwtProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class JwtTokenAdapter implements TokenGeneratorClient {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERMISSIONS = "permissions";
    private final JwtProperties jwtProperties;
    private final JwtDecoder keycloakDecoder;

    public JwtTokenAdapter(JwtProperties jwtProperties,
                           @Qualifier("keycloakJwtDecoder") JwtDecoder keycloakLocalDecoder) {
        this.jwtProperties = jwtProperties;
        this.keycloakDecoder = keycloakLocalDecoder;
    }


    // ── Interface methods ────────────────────────────────────────────────────

    @Override
    public AuthTokenCommand generate(String username, UUID userId, Set<String> roles, Set<String> permissions) {
        String accessToken  = generateAccessToken(username, userId, roles, permissions);
        String refreshToken = generateRefreshToken(username, userId, roles, permissions);
        return new AuthTokenCommand(accessToken, refreshToken, "Bearer", getExpiresInSeconds(), roles);
    }

    @Override
    public long getExpiresInSeconds() {
        return TimeUnit.MINUTES.toSeconds(jwtProperties.getAccessExpirationMinutes());
    }

    @Override
    public boolean isAccessTokenExpired(String token) {
        return extractExpiration(token, getAccessKey()).before(new Date());
    }

    @Override
    public boolean isRefreshTokenExpired(String token) {
        return extractExpiration(token, getRefreshKey()).before(new Date());
    }

    @Override
    public void validateAccessToken(String token) {
        validate(token, getAccessKey(), "Access token");
    }

    @Override
    public void validateRefreshToken(String token) {
        validate(token, getRefreshKey(), "Refresh token");
    }

    // ── Extract email ─────────────────────────────────────────────────────

    @Override
    public String extractEmailFromAccessToken(String token) {
        return extractClaim(token, Claims::getSubject, getAccessKey());
    }

    @Override
    public String extractEmailFromRefreshToken(String token) {
        return extractClaim(token, Claims::getSubject, getRefreshKey());
    }

    // ── Extract userId ───────────────────────────────────────────────────────

    @Override
    public UUID extractUserIdFromAccessToken(String token) {
        String raw = extractClaim(token, c -> c.get(CLAIM_USER_ID, String.class), getAccessKey());
        return UUID.fromString(raw);
    }

    @Override
    public UUID extractUserIdFromRefreshToken(String token) {
        String raw = extractClaim(token, c -> c.get(CLAIM_USER_ID, String.class), getRefreshKey());
        return UUID.fromString(raw);
    }

    // user provider database --> no prefix
    @Override
    public UUID extractUserIdFromKeycloakAccessToken(String keycloakAccessToken) {
        return UUID.fromString(keycloakDecoder.decode(keycloakAccessToken).getClaimAsString("sub"));
    }

    @Override
    public String extractEmailFromKeycloakAccessToken(String keycloakAccessToken) {
        return keycloakDecoder.decode(keycloakAccessToken).getClaimAsString("email");
    }

    @Override
    public Set<String> extractRolesFromAccessToken(String accessToken) {
        return extractStringSetClaim(accessToken, CLAIM_ROLES, getAccessKey());
    }

    @Override
    public Set<String> extractRolesFromRefreshToken(String refreshToken) {
        return extractStringSetClaim(refreshToken, CLAIM_ROLES, getRefreshKey());
    }

    @Override
    public Set<String> extractPermissionsFromRefreshToken(String refreshToken) {
        return extractStringSetClaim(refreshToken, CLAIM_PERMISSIONS, getRefreshKey());
    }


    // ── Private: generate ────────────────────────────────────────────────────

    private String generateAccessToken(String username, UUID userId, Set<String> roles, Set<String> permissions) {
        long expirationMs = TimeUnit.MINUTES.toMillis(jwtProperties.getAccessExpirationMinutes());
        return buildToken(username, userId, roles, permissions, getAccessKey(), expirationMs);
    }

    private String generateRefreshToken(String username, UUID userId, Set<String> roles, Set<String> permissions) {
        long expirationMs = TimeUnit.DAYS.toMillis(jwtProperties.getRefreshExpirationDay());
        return buildToken(username, userId, roles, permissions, getRefreshKey(), expirationMs);
    }

    private String buildToken(String username, UUID userId, Set<String> roles, Set<String> permissions, SecretKey key, long expirationMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_USER_ID, userId.toString())
                .claim(CLAIM_ROLES, normalizeClaims(roles))
                .claim(CLAIM_PERMISSIONS, normalizeClaims(permissions))
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(key)
                .compact();
    }

    // ── Private: validate & extract ──────────────────────────────────────────

    private void validate(String token, SecretKey key, String tokenLabel) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(tokenLabel + " has expired");
        } catch (JwtException e) {
            throw new UnauthorizedException(tokenLabel + " is invalid");
        }
    }

    private Date extractExpiration(String token, SecretKey key) {
        return extractClaim(token, Claims::getExpiration, key);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver, SecretKey key) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    private Set<String> extractStringSetClaim(String token, String claimName, SecretKey key) {
        List<?> values = extractClaim(token, claims -> claims.get(claimName, List.class), key);
        if (values == null || values.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> result = new LinkedHashSet<>();
        for (Object value : values) {
            if (value instanceof String str && !str.isBlank()) {
                result.add(str);
            }
        }
        return result;
    }

    private List<String> normalizeClaims(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                result.add(value);
            }
        }
        Collections.sort(result);
        return result;
    }

    // ── Private: keys ────────────────────────────────────────────────────────

    private SecretKey getAccessKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getAccessSecret()));
    }

    private SecretKey getRefreshKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getRefreshSecret()));
    }
}
