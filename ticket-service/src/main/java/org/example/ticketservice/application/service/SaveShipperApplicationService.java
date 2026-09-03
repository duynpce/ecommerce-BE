package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.example.ticketservice.application.command.SaveShipperApplicationCommand;
import org.example.ticketservice.application.repository.ShipperApplicationTicketRepository;
import org.example.ticketservice.application.usecase.SaveShipperApplicationUseCase;
import org.example.ticketservice.domain.exception.ConflictException;
import org.example.ticketservice.domain.model.ShipperApplicationTicket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveShipperApplicationService implements SaveShipperApplicationUseCase {

    private static final String PROCESS_KEY = "shipper-application-procedure";

    private final RuntimeService runtimeService;
    private final ShipperApplicationTicketRepository repository;

    @Override
    @Transactional
    public void execute(SaveShipperApplicationCommand command) {
        if (repository.existsPendingByUserId(command.userId())) {
            throw new ConflictException("You already have a pending shipper application.");
        }

        ShipperApplicationTicket ticket = new ShipperApplicationTicket();
        ticket.setUserId(command.userId());
        ticket.setIdentityCardNumber(command.identityCardNumber());
        ticket.setDriverLicenseNumber(command.driverLicenseNumber());
        ticket.setVehicleType(command.vehicleType());
        ticket.setVehiclePlateNumber(command.vehiclePlateNumber());
        ticket.setPhoneNumber(command.phoneNumber());
        ShipperApplicationTicket saved = repository.save(ticket);

        runtimeService.startProcessInstanceByKey(
                PROCESS_KEY,
                saved.getId().toString(),
                Map.of(
                        "shipperApplicationTicketId", saved.getId().toString(),
                        "userId", command.userId().toString()));
        log.info("Started shipper application process: ticketId={}, userId={}",
                saved.getId(), command.userId());
    }
}
