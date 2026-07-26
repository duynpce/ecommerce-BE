package org.example.userservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.userservice.domain.constant.Gender;

import java.util.UUID;
public record CreateAccountProfileRequest(

    @NotNull(message = "userId cannot be null")
    UUID userId,

    @NotBlank(message = "firstName cannot be blank")
    String firstName,

    @NotBlank(message = "lastName cannot be blank")
    String lastName,

    @NotBlank(message = "phoneNumber cannot be blank")
    String phoneNumber,

    @NotBlank(message = "address cannot be blank")
    String address,

    @NotNull(message = "gender cannot be null")
    Gender gender
    ){}