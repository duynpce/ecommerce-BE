package org.example.authservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import org.example.authservice.domain.constant.AccountStatus;

public record UpdateAccountStatusRequest(
        @NotNull(message = "status is required") AccountStatus status
) {}
