package org.example.userservice.application.mapper;

import org.example.userservice.application.command.CreateContributorProfileCommand;
import org.example.userservice.domain.model.ContributorProfile;
import org.example.userservice.infrastructure.web.data.entity.ContributorProfileEntity;
import org.example.userservice.infrastructure.web.dto.ContributorProfileResponse;
import org.example.userservice.infrastructure.web.dto.CreateContributorProfileRequest;

public interface ContributorProfileMapper {

    ContributorProfile toDomain(ContributorProfileEntity entity);

    ContributorProfile toDomain(CreateContributorProfileCommand command);

    CreateContributorProfileCommand toCommand(CreateContributorProfileRequest request);

    ContributorProfileEntity toEntity(ContributorProfile contributorProfile);

    ContributorProfileResponse toResponse(ContributorProfile contributorProfile);
}
