package org.example.authservice.infrastructure.web.dto;

import jakarta.validation.constraints.*;
import org.example.authservice.domain.constant.Gender;

public record RegisterRequest(

    @NotBlank
    @Size(min = 8, message = "Username must be at least 8 characters long")
        String username,

    @NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>/?\\\\|~`])\\S{8,}$",
            message = "password must contain at least one uppercase letter, one lowercase letter, one digit,one special character and its length must be equal or greater than 8")
    String password,

    @NotBlank
    @Email
    String email,

    @NotBlank
    String firstName,

    @NotBlank
    String lastName,

    @NotBlank
    String phoneNumber,

    @NotBlank
    String address,

    @NotNull
    Gender gender
)
{

}
