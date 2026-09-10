package org.example.ticketservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ticketservice.application.client.TokenGeneratorClient;
import org.example.ticketservice.application.usecase.DeliveryWorkUseCase;
import org.example.ticketservice.domain.exception.UnauthorizedException;
import org.example.ticketservice.domain.model.DeliveryWorkItem;
import org.example.ticketservice.infrastructure.web.dto.CompleteDeliveryRequest;
import org.example.ticketservice.infrastructure.web.dto.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryWorkController {
    private final DeliveryWorkUseCase useCase;
    private final TokenGeneratorClient tokenGeneratorClient;

    @GetMapping
    @PreAuthorize("hasAuthority('DELIVERY:READ_SELF')")
    public ResponseEntity<ResponseDto<List<DeliveryWorkItem>>> getWork(
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        return ResponseEntity.ok(ResponseDto.success(useCase.getWork(userId(accessToken))));
    }

    @PostMapping("/{taskId}/pickup")
    @PreAuthorize("hasAuthority('DELIVERY:WRITE_SELF')")
    public ResponseEntity<ResponseDto<Void>> acceptPickup(
            @PathVariable String taskId,
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        useCase.acceptPickup(taskId, userId(accessToken));
        return ResponseEntity.ok(ResponseDto.success(null, "Pickup accepted"));
    }

    @PostMapping("/{taskId}/outcome")
    @PreAuthorize("hasAuthority('DELIVERY:WRITE_SELF')")
    public ResponseEntity<ResponseDto<Void>> completeDelivery(
            @PathVariable String taskId,
            @Valid @RequestBody CompleteDeliveryRequest request,
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        useCase.completeDelivery(taskId, userId(accessToken), request.outcome());
        return ResponseEntity.ok(ResponseDto.success(null, "Delivery outcome recorded"));
    }

    private UUID userId(String accessToken) {
        if (accessToken == null) throw new UnauthorizedException("Unauthorized");
        return tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
    }
}
