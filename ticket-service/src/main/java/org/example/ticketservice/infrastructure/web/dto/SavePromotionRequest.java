package org.example.ticketservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SavePromotionRequest(

        @NotBlank
        String identityCardNumber,

        @NotBlank
        String bankName,

        @NotBlank
        String bankAccountNumber,

        @NotBlank
        String taxId
) {}
