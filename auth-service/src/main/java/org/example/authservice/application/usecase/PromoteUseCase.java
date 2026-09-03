package org.example.authservice.application.usecase;

public interface PromoteUseCase {
    void promoteToContributor(String userId);

    void promoteToShipper(String userId);
}
