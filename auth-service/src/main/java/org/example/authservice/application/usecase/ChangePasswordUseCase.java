package org.example.authservice.application.usecase;

import org.example.authservice.application.command.ChangePasswordCommand;

public interface ChangePasswordUseCase {
    void changePassword(ChangePasswordCommand command);
}
