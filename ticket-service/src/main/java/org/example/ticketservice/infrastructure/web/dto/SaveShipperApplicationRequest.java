package org.example.ticketservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.ticketservice.domain.constant.VehicleType;

public record SaveShipperApplicationRequest(
        @NotBlank @Size(max = 50) String identityCardNumber,
        @NotBlank @Size(max = 50) String driverLicenseNumber,
        @NotNull VehicleType vehicleType,
        @NotBlank @Size(max = 30) String vehiclePlateNumber,
        @NotBlank @Size(max = 25)
        @Pattern(regexp = "^[0-9+()\\-\\s]{7,25}$", message = "must be a valid phone number")
        String phoneNumber
) {}
