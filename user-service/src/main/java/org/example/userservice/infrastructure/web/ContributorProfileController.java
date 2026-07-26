package org.example.userservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.application.mapper.ContributorProfileMapper;
import org.example.userservice.application.usecase.ContributorProfileUseCase;
import org.example.userservice.domain.model.ContributorProfile;
import org.example.userservice.infrastructure.web.dto.ContributorProfileResponse;
import org.example.userservice.infrastructure.web.dto.CreateContributorProfileRequest;
import org.example.userservice.infrastructure.web.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/contributor-profiles")
@RequiredArgsConstructor
public class ContributorProfileController {

    private final ContributorProfileUseCase contributorProfileUseCase;
    private final ContributorProfileMapper contributorProfileMapper;

    @PostMapping
    public ResponseEntity<ResponseDto<Void>> createContributorProfile(
            @Valid @RequestBody CreateContributorProfileRequest request) {
        contributorProfileUseCase.createContributorProfile(contributorProfileMapper.toCommand(request));
        return new ResponseEntity<>(
                ResponseDto.success(null, "Contributor profile created successfully"),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<ResponseDto<ContributorProfileResponse>> getByAccountId(
            @PathVariable UUID accountId) {
        ContributorProfile contributorProfile = contributorProfileUseCase.getByAccountId(accountId);
        return ResponseEntity.ok(
                ResponseDto.success(contributorProfileMapper.toResponse(contributorProfile))
        );
    }

    @GetMapping("/accounts/{accountId}/exists")
    public ResponseEntity<ResponseDto<Boolean>> existsByAccountId(
            @PathVariable UUID accountId) {
        return ResponseEntity.ok(
                ResponseDto.success(contributorProfileUseCase.existsByAccountId(accountId))
        );
    }

    @GetMapping("/identity-card/{identityCardNumber}/exists")
    public ResponseEntity<ResponseDto<Boolean>> existsByIdentityCardNumber(
            @PathVariable String identityCardNumber) {
        return ResponseEntity.ok(
                ResponseDto.success(contributorProfileUseCase.existsByIdentityCardNumber(identityCardNumber))
        );
    }

    @GetMapping("/bank-account/{bankAccountNumber}/exists")
    public ResponseEntity<ResponseDto<Boolean>> existsByBankAccountNumber(
            @PathVariable String bankAccountNumber) {
        return ResponseEntity.ok(
                ResponseDto.success(contributorProfileUseCase.existsByBankAccountNumber(bankAccountNumber))
        );
    }

    @GetMapping("/tax-id/{taxId}/exists")
    public ResponseEntity<ResponseDto<Boolean>> existsByTaxId(
            @PathVariable String taxId) {
        return ResponseEntity.ok(
                ResponseDto.success(contributorProfileUseCase.existsByTaxId(taxId))
        );
    }
}
