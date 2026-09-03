package org.example.ticketservice.application.client;

public interface AuthClient {
    void promoteAccountToContributor(String userId);

    void promoteAccountToShipper(String userId);
}
