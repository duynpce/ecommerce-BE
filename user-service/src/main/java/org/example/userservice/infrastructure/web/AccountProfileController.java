package org.example.userservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.application.client.TokenGeneratorClient;
import org.example.userservice.application.command.PageCommand;
import org.example.userservice.application.criteria.AccountProfileSearchCriteria;
import org.example.userservice.application.mapper.AccountProfileMapper;
import org.example.userservice.application.usecase.AccountProfileUseCase;
import org.example.userservice.domain.model.AccountProfile;
import org.example.userservice.infrastructure.web.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/account-profiles")
@RequiredArgsConstructor
public class AccountProfileController {

    private final AccountProfileUseCase accountProfileUseCase;
    private final AccountProfileMapper accountProfileMapper;
    private final TokenGeneratorClient tokenGeneratorClient;

    @PostMapping
    public ResponseEntity<ResponseDto<Void>> createContributorAccount(@Valid @RequestBody CreateAccountProfileRequest request) {
        accountProfileUseCase.createAccount(accountProfileMapper.toCommand(request));
        return new ResponseEntity<>(ResponseDto.success(null, "Account created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/phone-number/{phoneNumber}")
    public ResponseEntity<ResponseDto<Boolean>> existPhoneNumber(@PathVariable("phoneNumber") String phoneNumber) {
        return ResponseEntity.ok(ResponseDto.success(accountProfileUseCase.existsByPhoneNumber(phoneNumber)));
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDto<AccountProfileResponse>> getMyAccountProfile(@CookieValue(required = false, value = "accessToken") String accessToken) {
        UUID userId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        AccountProfile accountProfile = accountProfileUseCase.getAccountProfileById(userId);
        return ResponseEntity.ok(ResponseDto.success(accountProfileMapper.toResponse(accountProfile)));
    }

    @PutMapping("/me")
    public ResponseEntity<ResponseDto<AccountProfileResponse>> updateMyAccountProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdateAccountProfileRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        AccountProfile updated = accountProfileUseCase.updateAccountProfile(userId, accountProfileMapper.toCommand(request));
        return ResponseEntity.ok(ResponseDto.success(accountProfileMapper.toResponse(updated), "Account profile updated successfully"));
    }

    /**
     * Filterable, paginated account report.
     * Example: GET /accounts/report?firstName=an&gender=FEMALE&createdFrom=2026-06-01&createdTo=2026-06-30&page=0&limit=20
     */
    @GetMapping("/report")
    @PreAuthorize("hasAuthority('EXPORT:READ_ALL')")
    public ResponseEntity<ResponseDto<List<AccountProfileReportResponsive>>> getAccountReport(
            @Valid @ModelAttribute AccountProfileReportFilter filter) {

        log.info(filter.toString());
        AccountProfileSearchCriteria criteria = accountProfileMapper.toCriteria(filter);

        PageCommand<AccountProfile> accountPage = accountProfileUseCase.getAccountReport(criteria);

        List<AccountProfileReportResponsive> data = accountPage.getContent().stream()
                .map(accountProfileMapper::toReportResponse)
                .toList();

        MetaDto metaDto = MetaDto.builder()
                .totalItems(accountPage.getTotalElements())
                .totalPages(accountPage.getTotalPages())
                .paginationDto(filter.getPaginationDto())
                .build();

        return ResponseEntity.ok(ResponseDto.success(data, "Account report fetched successfully", metaDto));
    }

}
