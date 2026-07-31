package org.example.authservice.application.usecase;

import org.example.authservice.application.command.AuthTokenCommand;
import org.example.authservice.application.command.CompleteProfileCommand;

public interface CompleteProfileUseCase {
    AuthTokenCommand completeProfile(CompleteProfileCommand  command);
}
