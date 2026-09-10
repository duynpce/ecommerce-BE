package org.example.authservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.authservice.application.usecase.AdminAccountUseCase;
import org.example.authservice.domain.constant.AccountStatus;
import org.example.authservice.infrastructure.web.dto.ResponseDto;
import org.example.authservice.infrastructure.web.dto.UpdateAccountRolesRequest;
import org.example.authservice.infrastructure.web.dto.UpdateAccountStatusRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/local/admin/accounts")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('USER:UPDATE_ALL') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminAccountController {

    private final AdminAccountUseCase adminAccountUseCase;

    @GetMapping("/statuses")
    public ResponseEntity<ResponseDto<Map<UUID, AccountStatus>>> getStatuses(
            @RequestParam List<UUID> ids) {
        return ResponseEntity.ok(ResponseDto.success(adminAccountUseCase.getStatuses(ids)));
    }

    @GetMapping("/roles")
    public ResponseEntity<ResponseDto<Map<UUID, Set<String>>>> getRoles(
            @RequestParam List<UUID> ids) {
        return ResponseEntity.ok(ResponseDto.success(adminAccountUseCase.getRoles(ids)));
    }

    @PutMapping("/{accountId}/status")
    public ResponseEntity<ResponseDto<AccountStatus>> updateStatus(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateAccountStatusRequest request) {
        AccountStatus status = adminAccountUseCase.updateStatus(
                accountId,
                UUID.fromString(jwt.getClaimAsString("userId")),
                request.status()
        );
        return ResponseEntity.ok(ResponseDto.success(status, "Account status updated successfully"));
    }

    @PutMapping("/{accountId}/roles")
    @PreAuthorize("hasAuthority('PROMOTE:WRITE_ALL') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ResponseDto<Set<String>>> updateRoles(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateAccountRolesRequest request) {
        Set<String> roles = adminAccountUseCase.updateRoles(
                accountId,
                UUID.fromString(jwt.getClaimAsString("userId")),
                request.roles()
        );
        return ResponseEntity.ok(ResponseDto.success(roles, "Account permissions updated successfully"));
    }
}
