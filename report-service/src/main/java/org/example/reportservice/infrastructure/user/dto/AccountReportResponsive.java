package org.example.reportservice.infrastructure.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.reportservice.domain.constant.Gender;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class AccountReportResponsive {
    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final String address;
    private final Gender gender;
    private final Instant createdAt;
    private final Instant updatedAt;

}
