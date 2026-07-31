package org.example.userservice.infrastructure.web.data.adapter;

import lombok.RequiredArgsConstructor;
import org.example.userservice.application.command.CreateContributorProfileCommand;
import org.example.userservice.application.mapper.ContributorProfileMapper;
import org.example.userservice.domain.model.ContributorProfile;
import org.example.userservice.infrastructure.web.data.entity.ContributorProfileEntity;
import org.example.userservice.infrastructure.mapper.ContributorProfileMapperMapstruct;
import org.example.userservice.infrastructure.web.dto.ContributorProfileResponse;
import org.example.userservice.infrastructure.web.dto.CreateContributorProfileRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContributorProfileMapperAdapter implements ContributorProfileMapper {

    private final ContributorProfileMapperMapstruct contributorProfileMapperMapstruct;

    @Override
    public ContributorProfile toDomain(ContributorProfileEntity entity) {
        return contributorProfileMapperMapstruct.toDomain(entity);
    }

    @Override
    public ContributorProfile toDomain(CreateContributorProfileCommand command) {
        return contributorProfileMapperMapstruct.toDomain(command);
    }

    @Override
    public CreateContributorProfileCommand toCommand(CreateContributorProfileRequest request) {
        return contributorProfileMapperMapstruct.toCommand(request);
    }

    @Override
    public ContributorProfileEntity toEntity(ContributorProfile contributorProfile) {
        return contributorProfileMapperMapstruct.toEntity(contributorProfile);
    }

    @Override
    public ContributorProfileResponse toResponse(ContributorProfile contributorProfile) {
        return contributorProfileMapperMapstruct.toResponse(contributorProfile);
    }
}
