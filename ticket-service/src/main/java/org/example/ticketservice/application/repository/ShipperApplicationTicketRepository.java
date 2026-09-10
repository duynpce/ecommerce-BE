package org.example.ticketservice.application.repository;

import org.example.ticketservice.domain.model.ShipperApplicationTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ShipperApplicationTicketRepository {
    ShipperApplicationTicket save(ShipperApplicationTicket ticket);
    ShipperApplicationTicket findById(UUID id);
    Page<ShipperApplicationTicket> findAll(Pageable pageable);
    boolean existsPendingByUserId(UUID userId);
}
