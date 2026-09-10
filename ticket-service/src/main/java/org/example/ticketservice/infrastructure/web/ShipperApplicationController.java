package org.example.ticketservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ticketservice.application.client.TokenGeneratorClient;
import org.example.ticketservice.application.command.*;
import org.example.ticketservice.application.usecase.*;
import org.example.ticketservice.domain.exception.UnauthorizedException;
import org.example.ticketservice.domain.model.ShipperApplicationTicket;
import org.example.ticketservice.infrastructure.web.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/shipper-applications")
@RequiredArgsConstructor
public class ShipperApplicationController {

    private final SaveShipperApplicationUseCase saveUseCase;
    private final GetShipperApplicationsUseCase getUseCase;
    private final ApproveShipperApplicationUseCase approveUseCase;
    private final RejectShipperApplicationUseCase rejectUseCase;
    private final TokenGeneratorClient tokenGeneratorClient;

    @PostMapping
    @PreAuthorize("hasAuthority('TICKET:WRITE_SELF')")
    public ResponseEntity<Void> save(
            @Valid @RequestBody SaveShipperApplicationRequest request,
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        if (accessToken == null) throw new UnauthorizedException("Unauthorized");
        UUID userId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);
        saveUseCase.execute(new SaveShipperApplicationCommand(
                userId,
                request.identityCardNumber(),
                request.driverLicenseNumber(),
                request.vehicleType(),
                request.vehiclePlateNumber(),
                request.phoneNumber()));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TICKET:READ_ALL')")
    public ResponseEntity<ResponseDto<List<ShipperApplicationResponse>>> getAll(
            @Valid PaginationDto pagination) {
        Page<ShipperApplicationTicket> page = getUseCase.execute(PageRequest.of(
                pagination.getPage(), pagination.getLimit(), Sort.by("ticket.createdAt").descending()));
        List<ShipperApplicationResponse> data = page.getContent().stream()
                .map(this::toResponse)
                .toList();
        MetaDto meta = MetaDto.builder()
                .paginationDto(pagination)
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
        return ResponseEntity.ok(ResponseDto.success(data, "Fetched successfully", meta));
    }

    @PostMapping("/{ticketId}/approve")
    @PreAuthorize("hasAuthority('TICKET:WRITE_ALL')")
    public ResponseEntity<Void> approve(@PathVariable UUID ticketId) {
        approveUseCase.approve(new ApproveShipperApplicationCommand(ticketId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{ticketId}/reject")
    @PreAuthorize("hasAuthority('TICKET:WRITE_ALL')")
    public ResponseEntity<Void> reject(@PathVariable UUID ticketId) {
        rejectUseCase.reject(new RejectShipperApplicationCommand(ticketId));
        return ResponseEntity.ok().build();
    }

    private ShipperApplicationResponse toResponse(ShipperApplicationTicket ticket) {
        return new ShipperApplicationResponse(
                ticket.getId(), ticket.getUserId(), ticket.getType(), ticket.getStatus(),
                ticket.getCreatedAt(), ticket.getIdentityCardNumber(),
                ticket.getDriverLicenseNumber(), ticket.getVehicleType(),
                ticket.getVehiclePlateNumber(), ticket.getPhoneNumber());
    }
}
