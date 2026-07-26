package org.example.ticketservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateContributorProfileRequest {

    @NotNull(message = "Account ID cannot be null")
    private UUID accountId;

    @NotBlank(message = "Identity card number is required")
    private String identityCardNumber;

    @NotBlank(message = "Bank account number is required")
    private String bankAccountNumber;

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Tax ID is required")
    private String taxId;
}
