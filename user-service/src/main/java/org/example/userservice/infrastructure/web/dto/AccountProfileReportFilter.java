package org.example.userservice.infrastructure.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.example.userservice.domain.constant.Gender;

import java.time.LocalDate;

/**
 * Bound from query params on GET /accounts/report.
 * All filter fields are optional; page/limit have sane defaults.
 */
public record AccountProfileReportFilter(

    String firstName,
    String lastName,
    Gender gender,
    String phoneNumber,
    LocalDate createdFrom,
    LocalDate createdTo,

    @NotNull(message = "page is required")
    @Min(value = 0, message = "page cannot be negative")
    Integer page ,

    @NotNull(message = "limit is required")
    @Min(value = 1, message = "limit must be greater than 0")
    Integer limit
    ) {
    public PaginationDto getPaginationDto() {
        return new PaginationDto(page, limit);
    }
}
