package org.example.authservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record UpdateAccountRolesRequest(
        @NotEmpty(message = "roles must contain at least one role")
        Set<@NotBlank(message = "role must not be blank") String> roles
) {
}
