package org.example.ticketservice.application.usecase;

import org.example.ticketservice.domain.model.ShipperApplicationTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetShipperApplicationsUseCase {
    Page<ShipperApplicationTicket> execute(Pageable pageable);
}
