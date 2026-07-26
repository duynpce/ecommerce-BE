package org.example.userservice.infrastructure.mapper;

import org.example.userservice.application.command.CreateContributorProfileCommand;
import org.example.userservice.domain.model.ContributorProfile;
import org.example.userservice.infrastructure.web.data.entity.ContributorProfileEntity;
import org.example.userservice.infrastructure.web.dto.ContributorProfileResponse;
import org.example.userservice.infrastructure.web.dto.CreateContributorProfileRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ContributorProfileMapperMapstruct {

    ContributorProfile toDomain(ContributorProfileEntity entity);

    ContributorProfile toDomain(CreateContributorProfileCommand command);

    @Mapping(source = "accountId", target = "accountId")
    CreateContributorProfileCommand toCommand(CreateContributorProfileRequest request);

    ContributorProfileEntity toEntity(ContributorProfile contributorProfile);

    ContributorProfileResponse toResponse(ContributorProfile contributorProfile);
}
