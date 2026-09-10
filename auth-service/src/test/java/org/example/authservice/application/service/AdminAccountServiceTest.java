package org.example.authservice.application.service;

import org.example.authservice.application.repository.AccountCredentialRepository;
import org.example.authservice.application.repository.AuthTokenRepository;
import org.example.authservice.application.repository.RoleRepository;
import org.example.authservice.domain.constant.AccountStatus;
import org.example.authservice.domain.exception.ValidationException;
import org.example.authservice.domain.model.AccountCredential;
import org.example.authservice.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AdminAccountServiceTest {

    @Mock
    private AccountCredentialRepository accountCredentialRepository;
    @Mock
    private AuthTokenRepository authTokenRepository;
    @Mock
    private RoleRepository roleRepository;

    private AdminAccountService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AdminAccountService(accountCredentialRepository, authTokenRepository, roleRepository);
    }

    @Test
    void updateStatusPersistsAndRevokesRefreshSession() {
        UUID accountId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        AccountCredential account = new AccountCredential();
        account.setId(accountId);
        when(accountCredentialRepository.findById(accountId)).thenReturn(Optional.of(account));

        AccountStatus result = service.updateStatus(accountId, adminId, AccountStatus.BLOCKED);

        assertEquals(AccountStatus.BLOCKED, result);
        assertEquals(AccountStatus.BLOCKED, account.getStatus());
        verify(accountCredentialRepository).save(account);
        verify(authTokenRepository).deleteByUserId(accountId);
    }

    @Test
    void updateStatusRejectsSelfBan() {
        UUID adminId = UUID.randomUUID();

        assertThrows(
                ValidationException.class,
                () -> service.updateStatus(adminId, adminId, AccountStatus.BLOCKED)
        );

        verifyNoInteractions(accountCredentialRepository, authTokenRepository);
    }

    @Test
    void updateRolesPersistsAssignableRolesAndRevokesRefreshSession() {
        UUID accountId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        AccountCredential account = new AccountCredential();
        account.setId(accountId);
        Role customer = role("CUSTOMER");
        Role contributor = role("CONTRIBUTOR");
        Role shipper = role("SHIPPER");
        account.setRoles(Set.of(customer));
        when(accountCredentialRepository.findByIdWithRolesAndPermissions(accountId)).thenReturn(account);
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customer));
        when(roleRepository.findByName("CONTRIBUTOR")).thenReturn(Optional.of(contributor));
        when(roleRepository.findByName("SHIPPER")).thenReturn(Optional.of(shipper));

        Set<String> result = service.updateRoles(
                accountId,
                adminId,
                Set.of("customer", "CONTRIBUTOR", "shipper")
        );

        assertEquals(Set.of("CUSTOMER", "CONTRIBUTOR", "SHIPPER"), result);
        assertEquals(Set.of("CUSTOMER", "CONTRIBUTOR", "SHIPPER"), account.extractRoleNames());
        verify(accountCredentialRepository).save(account);
        verify(authTokenRepository).deleteByUserId(accountId);
    }

    @Test
    void updateRolesRejectsPrivilegeEscalation() {
        UUID accountId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        assertThrows(
                ValidationException.class,
                () -> service.updateRoles(accountId, adminId, Set.of("ADMIN"))
        );

        verifyNoInteractions(accountCredentialRepository, authTokenRepository, roleRepository);
    }

    @Test
    void updateRolesRejectsChangesToAdminAccounts() {
        UUID accountId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        AccountCredential account = new AccountCredential();
        account.setId(accountId);
        account.setRoles(Set.of(role("ADMIN")));
        when(accountCredentialRepository.findByIdWithRolesAndPermissions(accountId)).thenReturn(account);

        assertThrows(
                ValidationException.class,
                () -> service.updateRoles(accountId, adminId, Set.of("CUSTOMER"))
        );

        verify(accountCredentialRepository, never()).save(any());
        verifyNoInteractions(authTokenRepository, roleRepository);
    }

    private Role role(String name) {
        Role role = new Role();
        role.setName(name);
        role.setPermissions(Set.of());
        return role;
    }
}
