package org.example.ticketservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ticketservice.application.client.TokenGeneratorClient;
import org.example.ticketservice.application.usecase.ReturnWorkUseCase;
import org.example.ticketservice.domain.exception.UnauthorizedException;
import org.example.ticketservice.domain.model.ReturnWorkItem;
import org.example.ticketservice.infrastructure.web.dto.ConfirmReturnWorkRequest;
import org.example.ticketservice.infrastructure.web.dto.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/returns")
@RequiredArgsConstructor
public class ReturnWorkController {
    private final ReturnWorkUseCase useCase;
    private final TokenGeneratorClient tokenGeneratorClient;

    @GetMapping
    @PreAuthorize("hasAuthority('TRANSACTION:READ_SELF')")
    public ResponseEntity<ResponseDto<List<ReturnWorkItem>>> getPendingReturns(
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        return ResponseEntity.ok(ResponseDto.success(useCase.getPendingReturns(userId(accessToken))));
    }

    @PostMapping("/{taskId}/confirm")
    @PreAuthorize("hasAuthority('TRANSACTION:WRITE_SELF')")
    public ResponseEntity<ResponseDto<Void>> confirm(
            @PathVariable String taskId,
            @Valid @RequestBody ConfirmReturnWorkRequest request,
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        useCase.confirmReturn(taskId, userId(accessToken), request.received());
        return ResponseEntity.ok(ResponseDto.success(null, "Return status recorded"));
    }

    private UUID userId(String accessToken) {
        if (accessToken == null) throw new UnauthorizedException("Unauthorized");
        return tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
    }
}
