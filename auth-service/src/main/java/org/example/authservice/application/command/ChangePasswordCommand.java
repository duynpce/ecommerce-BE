package org.example.authservice.application.command;

import java.util.UUID;

public record ChangePasswordCommand(
        UUID   accountId,
        String currentPassword,
        String newPassword
) {}
