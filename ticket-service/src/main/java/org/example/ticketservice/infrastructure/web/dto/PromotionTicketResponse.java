package org.example.ticketservice.infrastructure.web.dto;

import org.example.ticketservice.domain.constant.TicketStatus;

import java.time.Instant;
import java.util.UUID;

public record PromotionTicketResponse(
        UUID         ticketId,
        UUID         userId,
        TicketStatus status,
        Instant      createdAt,
        String       identityCardNumber,
        String       bankName,
        String       bankAccountNumber,
        String       taxId
) {}
