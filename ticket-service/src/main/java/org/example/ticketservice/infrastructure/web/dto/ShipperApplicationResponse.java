package org.example.ticketservice.infrastructure.web.dto;

import org.example.ticketservice.domain.constant.TicketStatus;
import org.example.ticketservice.domain.constant.TicketType;
import org.example.ticketservice.domain.constant.VehicleType;

import java.time.Instant;
import java.util.UUID;

public record ShipperApplicationResponse(
        UUID ticketId,
        UUID userId,
        TicketType type,
        TicketStatus status,
        Instant createdAt,
        String identityCardNumber,
        String driverLicenseNumber,
        VehicleType vehicleType,
        String vehiclePlateNumber,
        String phoneNumber
) {}
