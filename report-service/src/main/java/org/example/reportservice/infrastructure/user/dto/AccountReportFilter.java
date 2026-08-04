package org.example.reportservice.infrastructure.user.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.example.reportservice.domain.constant.ExportFileName;
import org.example.reportservice.domain.constant.Gender;

import java.time.LocalDate;

/**
 * Bound from query params on GET /accounts/report.
 * All filter fields are optional; page/limit have sane defaults.
 */
public record AccountReportFilter(
        String firstName,
        String lastName,
        Gender gender,
        String phoneNumber,
        LocalDate createdFrom,
        LocalDate createdTo,

        @NotNull(message = "exportFileName is required")
        ExportFileName exportFileName,
        @NotNull(message = "page is required")
        @Min(value = 0, message = "page cannot be negative")
        Integer page,
        @NotNull(message = "limit is required")
        @Min(value = 1, message = "limit must be greater than 0")
        Integer limit
) {
}
