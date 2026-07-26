package org.example.userservice.application.command;

import org.example.userservice.domain.constant.Gender;

import java.util.UUID;

public record CreateProfileAccountCommand(
        UUID id,
        String firstName,
        String lastName,
        String phoneNumber,
        String address,
        Gender gender
) {
    public CreateProfileAccountCommand {
        if (id == null) throw new IllegalArgumentException("Id cannot be null.");
        if (firstName == null) throw new IllegalArgumentException("First name cannot be null.");
        if (lastName == null) throw new IllegalArgumentException("Last name cannot be null.");
        if (phoneNumber == null) throw new IllegalArgumentException("Phone number cannot be null.");
        if (address == null) throw new IllegalArgumentException("Address cannot be null.");
        if (gender == null) throw new IllegalArgumentException("Gender cannot be null.");
    }
}