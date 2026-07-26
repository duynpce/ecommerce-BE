package org.example.ticketservice.infrastructure.user;

import lombok.RequiredArgsConstructor;
import org.example.ticketservice.application.client.UserClient;
import org.example.ticketservice.infrastructure.web.dto.CreateContributorProfileRequest;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserClientAdapter implements UserClient {

    private final UserHttpClient userHttpClient;

    @Override
    public void createContributorProfile(CreateContributorProfileRequest request) {
        userHttpClient.createContributorProfile(request);
    }
}
