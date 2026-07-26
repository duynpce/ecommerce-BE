// application/command/ResetKeycloakPasswordCommand.java
package org.example.authservice.application.command;

import java.util.UUID;

public record ResetKeycloakPasswordCommand(
        UUID    keycloakUserId,
        String  newPassword,
        boolean temporary
) {
    /** Convenience factory — permanent password (temporary = false). */
    public static ResetKeycloakPasswordCommand permanent(UUID keycloakUserId, String newPassword) {
        return new ResetKeycloakPasswordCommand(keycloakUserId, newPassword, false);
    }
}