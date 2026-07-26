package org.example.authservice.application.command;

import org.example.authservice.domain.constant.Gender;

public record CompleteProfileCommand(
        String username,
        String password,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String address,
        Gender gender
) {
    void validate() {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address is required");
        }

        if (gender == null) {
            throw new IllegalArgumentException("Gender is required");
        }
    }

}
