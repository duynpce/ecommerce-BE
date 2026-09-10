package org.example.userservice.infrastructure.web.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.userservice.domain.constant.Gender;

public record UpdateAccountProfileRequest(
    @Size(max = 100, message = "firstName cannot exceed 100 characters")
    String firstName,
    @Size(max = 100, message = "lastName cannot exceed 100 characters")
    String lastName,
    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "phoneNumber must contain 8 to 15 digits and may start with +")
    String phoneNumber,
    @Size(max = 255, message = "address cannot exceed 255 characters")
    String address,
    Gender gender
) {}
