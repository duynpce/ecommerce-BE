package org.example.authservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.authservice.application.repository.AccountCredentialRepository;
import org.example.authservice.application.repository.AuthTokenRepository;
import org.example.authservice.application.repository.RoleRepository;
import org.example.authservice.application.usecase.AdminAccountUseCase;
import org.example.authservice.domain.constant.AccountStatus;
import org.example.authservice.domain.exception.NotFoundException;
import org.example.authservice.domain.exception.ValidationException;
import org.example.authservice.domain.model.AccountCredential;
import org.example.authservice.domain.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAccountService implements AdminAccountUseCase {

    private static final Set<String> ADMIN_ASSIGNABLE_ROLES =
            Set.of("CUSTOMER", "CONTRIBUTOR", "SHIPPER");

    private final AccountCredentialRepository accountCredentialRepository;
    private final AuthTokenRepository authTokenRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, AccountStatus> getStatuses(Collection<UUID> accountIds) {
        Map<UUID, AccountStatus> result = new LinkedHashMap<>();
        accountCredentialRepository.findAllById(accountIds)
                .forEach(account -> result.put(account.getId(), account.getStatus()));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Set<String>> getRoles(Collection<UUID> accountIds) {
        Map<UUID, Set<String>> result = new LinkedHashMap<>();
        accountIds.forEach(accountId -> {
            AccountCredential account = accountCredentialRepository.findByIdWithRolesAndPermissions(accountId);
            result.put(accountId, account.extractRoleNames());
        });
        return result;
    }

    @Override
    @Transactional
    public AccountStatus updateStatus(UUID accountId, UUID adminId, AccountStatus status) {
        if (accountId.equals(adminId)) {
            throw new ValidationException("You cannot change your own account status");
        }
        if (status != AccountStatus.ACTIVE
                && status != AccountStatus.LIMITED
                && status != AccountStatus.BLOCKED) {
            throw new ValidationException("Admins may only set ACTIVE, LIMITED, or BLOCKED status");
        }

        AccountCredential account = accountCredentialRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));
        account.setStatus(status);
        accountCredentialRepository.save(account);

        // Existing access tokens expire quickly; removing the refresh session prevents
        // a blocked or newly limited user from extending old permissions.
        authTokenRepository.deleteByUserId(accountId);
        return status;
    }

    @Override
    @Transactional
    public Set<String> updateRoles(UUID accountId, UUID adminId, Set<String> roleNames) {
        if (accountId.equals(adminId)) {
            throw new ValidationException("You cannot change your own roles");
        }

        Set<String> normalizedRoleNames = roleNames.stream()
                .map(roleName -> roleName.trim().toUpperCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        if (normalizedRoleNames.isEmpty()) {
            throw new ValidationException("At least one role is required");
        }
        if (!ADMIN_ASSIGNABLE_ROLES.containsAll(normalizedRoleNames)) {
            throw new ValidationException(
                    "Admins may only assign CUSTOMER, CONTRIBUTOR, or SHIPPER roles");
        }

        AccountCredential account = accountCredentialRepository.findByIdWithRolesAndPermissions(accountId);
        if (account.extractRoleNames().stream().anyMatch(role -> !ADMIN_ASSIGNABLE_ROLES.contains(role))) {
            throw new ValidationException("Admin and super-admin roles cannot be changed here");
        }

        Set<Role> roles = normalizedRoleNames.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new NotFoundException("Role not found: " + roleName)))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        account.setRoles(roles);
        accountCredentialRepository.save(account);

        // Force the affected user to sign in again so newly granted or revoked
        // permissions are reflected in their next access token.
        authTokenRepository.deleteByUserId(accountId);
        return account.extractRoleNames();
    }
}
