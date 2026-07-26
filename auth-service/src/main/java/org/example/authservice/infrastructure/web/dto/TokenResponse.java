package org.example.authservice.infrastructure.web.dto;


public record TokenResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {}
