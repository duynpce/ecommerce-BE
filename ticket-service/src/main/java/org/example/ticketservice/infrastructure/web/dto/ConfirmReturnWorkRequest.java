package org.example.ticketservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

public record ConfirmReturnWorkRequest(@NotNull Boolean received) {}
