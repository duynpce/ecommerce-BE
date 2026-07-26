package org.example.authservice.infrastructure.web.data.springdata;

import org.example.authservice.infrastructure.web.entity.AccountCredentialEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import java.util.UUID;

public interface SpringDataAccountCredentialRepository extends JpaRepository<AccountCredentialEntity, UUID> {
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<AccountCredentialEntity> findWithRolesById(UUID id);
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<AccountCredentialEntity> findWithRolesByKeycloakId(UUID keycloakId);
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<AccountCredentialEntity> findByUsername(String username);
    @EntityGraph(attributePaths = {"roles", "roles.permissions"})
    Optional<AccountCredentialEntity> findByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}