package org.example.ticketservice.application.command;

import java.util.UUID;

public record SavePromotionRequestCommand(
        UUID   ticketId,
        String userId,
        String identityCardNumber,
        String bankName,
        String bankAccountNumber,
        String taxId
) {}
