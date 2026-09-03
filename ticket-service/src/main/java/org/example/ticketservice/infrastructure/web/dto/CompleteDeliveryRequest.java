package org.example.ticketservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CompleteDeliveryRequest(@NotBlank String outcome) {}
