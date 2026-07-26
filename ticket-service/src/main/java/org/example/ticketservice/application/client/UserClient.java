package org.example.ticketservice.application.client;


import org.example.ticketservice.infrastructure.web.dto.CreateContributorProfileRequest;

public interface UserClient {
    void createContributorProfile(CreateContributorProfileRequest request);
}
