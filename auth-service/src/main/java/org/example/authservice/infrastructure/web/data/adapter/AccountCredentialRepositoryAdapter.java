package org.example.authservice.infrastructure.web.data.adapter;

import lombok.RequiredArgsConstructor;
import org.example.authservice.application.mapper.AuthMapper;
import org.example.authservice.application.repository.AccountCredentialRepository;
import org.example.authservice.domain.exception.NotFoundException;
import org.example.authservice.domain.exception.UnauthorizedException;
import org.example.authservice.domain.model.AccountCredential;
import org.example.authservice.infrastructure.web.data.springdata.SpringDataAccountCredentialRepository;
import org.example.authservice.infrastructure.web.entity.AccountCredentialEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountCredentialRepositoryAdapter implements AccountCredentialRepository {

    private final SpringDataAccountCredentialRepository springDataRepo;
    private final AuthMapper mapper;

    @Override
    public AccountCredential save(AccountCredential domain) {
        AccountCredentialEntity entity;
        entity =  mapper.toEntity(domain);

        return mapper.toDomain(springDataRepo.save(entity));
    }

    @Override
    public Optional<AccountCredential> findById(UUID id) {
        return springDataRepo.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public AccountCredential findByIdWithRolesAndPermissions(UUID id) {
        return springDataRepo.findWithRolesById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new UnauthorizedException("Account credential not found for id: " + id));
    }

    @Override
    public AccountCredential findByKeycloakIdWithRolesAndPermissions(UUID keycloakId) {
        return springDataRepo.findWithRolesByKeycloakId(keycloakId)
                .map(mapper::toDomain)
                .orElseThrow(() -> new NotFoundException("Account credential not found for keycloakId: " + keycloakId));
    }

    @Override
    public Optional<AccountCredential> findByUsername(String username) {
        return springDataRepo.findByUsername(username)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<AccountCredential> findByEmail(String email) {
        return springDataRepo.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public List<AccountCredential> findAllById(Collection<UUID> ids) {
        return springDataRepo.findAllById(ids).stream()
                .map(mapper::toDomain)
                .toList();
    }


    @Override
    public boolean existsByUsername(String username) {
        return springDataRepo.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataRepo.existsByEmail(email);
    }

    @Override
    public void delete(UUID id) {
        if (!springDataRepo.existsById(id)) {
            throw new UnauthorizedException("Account credential not found for id: " + id);
        }
        springDataRepo.deleteById(id);
    }
}
