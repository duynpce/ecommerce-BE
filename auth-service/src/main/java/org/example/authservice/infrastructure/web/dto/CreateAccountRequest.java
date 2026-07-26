package org.example.authservice.infrastructure.web.dto;

import lombok.Data;
import org.example.authservice.domain.constant.Gender;

import java.util.UUID;

@Data
public class CreateAccountRequest {
    UUID userId;
    String firstName;
    String lastName;
    String phoneNumber;
    String address;
    Gender gender;
}
