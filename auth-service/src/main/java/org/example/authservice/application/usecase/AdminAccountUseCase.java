package org.example.authservice.application.usecase;

import org.example.authservice.domain.constant.AccountStatus;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface AdminAccountUseCase {
    Map<UUID, AccountStatus> getStatuses(Collection<UUID> accountIds);

    Map<UUID, Set<String>> getRoles(Collection<UUID> accountIds);

    AccountStatus updateStatus(UUID accountId, UUID adminId, AccountStatus status);

    Set<String> updateRoles(UUID accountId, UUID adminId, Set<String> roleNames);
}
