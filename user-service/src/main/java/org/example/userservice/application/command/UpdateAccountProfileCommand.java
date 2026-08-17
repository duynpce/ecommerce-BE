package org.example.userservice.application.command;

import org.example.userservice.domain.constant.Gender;

public record UpdateAccountProfileCommand(
    String firstName,
    String lastName,
    String phoneNumber,
    String address,
    Gender gender
) {}
