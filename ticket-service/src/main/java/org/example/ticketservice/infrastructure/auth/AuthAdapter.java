package org.example.ticketservice.infrastructure.auth;

import lombok.RequiredArgsConstructor;
import org.example.ticketservice.application.client.AuthClient;
import org.example.ticketservice.infrastructure.auth.httpclient.AuthHttpClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthAdapter implements AuthClient {

    private final AuthHttpClient authHttpClient;

    @Override
    public void promoteAccountToContributor(String userId) {
        authHttpClient.promoteAccountToContributor(userId);
    }

    @Override
    public void promoteAccountToShipper(String userId) {
        authHttpClient.promoteAccountToShipper(userId);
    }
}
