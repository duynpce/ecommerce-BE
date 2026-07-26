package org.example.userservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateContributorProfileRequest(

        @NotNull(message = "accountId cannot be null")
        UUID accountId,

        @NotBlank(message = "identityCardNumber cannot be blank")
        String identityCardNumber,

        @NotBlank(message = "bankName cannot be blank")
        String bankName,

        @NotBlank(message = "bankAccountNumber cannot be blank")
        String bankAccountNumber,

        @NotBlank(message = "taxId cannot be blank")
        String taxId
) {}
