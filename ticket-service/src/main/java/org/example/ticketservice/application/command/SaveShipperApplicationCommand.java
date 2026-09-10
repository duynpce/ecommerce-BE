package org.example.ticketservice.application.command;

import org.example.ticketservice.domain.constant.VehicleType;

import java.util.UUID;

public record SaveShipperApplicationCommand(
        UUID userId,
        String identityCardNumber,
        String driverLicenseNumber,
        VehicleType vehicleType,
        String vehiclePlateNumber,
        String phoneNumber
) {}
