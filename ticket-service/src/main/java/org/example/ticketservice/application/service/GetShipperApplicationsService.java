package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.ticketservice.application.repository.ShipperApplicationTicketRepository;
import org.example.ticketservice.application.usecase.GetShipperApplicationsUseCase;
import org.example.ticketservice.domain.model.ShipperApplicationTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetShipperApplicationsService implements GetShipperApplicationsUseCase {
    private final ShipperApplicationTicketRepository repository;

    @Override
    public Page<ShipperApplicationTicket> execute(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
