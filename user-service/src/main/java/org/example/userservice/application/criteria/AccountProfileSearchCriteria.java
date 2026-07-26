package org.example.userservice.application.criteria;

import org.example.userservice.domain.constant.Gender;

import java.time.Instant;

/**
 * Filter-only fields. Pagination/sorting is handled separately via Spring's Pageable.
 */
public record AccountProfileSearchCriteria(
        String firstName,
        String lastName,
        Gender gender,
        String phoneNumber,
        Instant createdFrom,
        Instant createdTo,
        Integer page,
        Integer limit
) {
}
