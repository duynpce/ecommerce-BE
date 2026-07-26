package org.example.userservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/account-profiles")
@RequiredArgsConstructor
public class AccountProfileController {

    private final AccountProfileUseCase accountProfileUseCase;
    private final AccountProfileMapper accountProfileMapper;

    @PostMapping
    public ResponseEntity<ResponseDto<Void>> createContributorAccount(@Valid @RequestBody CreateAccountProfileRequest request) {
        accountProfileUseCase.createAccount(accountProfileMapper.toCommand(request));
        return new ResponseEntity<>(ResponseDto.success(null, "Account created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/phone-number/{phoneNumber}")
    public ResponseEntity<ResponseDto<Boolean>> existPhoneNumber(@PathVariable("phoneNumber") String phoneNumber) {
        return ResponseEntity.ok(ResponseDto.success(accountProfileUseCase.existsByPhoneNumber(phoneNumber)));
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
