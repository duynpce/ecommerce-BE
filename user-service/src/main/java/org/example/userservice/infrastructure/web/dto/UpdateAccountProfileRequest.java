package org.example.userservice.infrastructure.web.dto;

import org.example.userservice.domain.constant.Gender;

public record UpdateAccountProfileRequest(
    String firstName,
    String lastName,
    String phoneNumber,
    String address,
    Gender gender
) {}
