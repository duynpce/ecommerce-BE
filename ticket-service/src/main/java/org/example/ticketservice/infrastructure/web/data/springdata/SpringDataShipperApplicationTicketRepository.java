package org.example.ticketservice.infrastructure.web.data.springdata;

import org.example.ticketservice.domain.constant.TicketStatus;
import org.example.ticketservice.infrastructure.web.entity.ShipperApplicationTicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataShipperApplicationTicketRepository
        extends JpaRepository<ShipperApplicationTicketEntity, UUID> {

    @Query("SELECT s FROM ShipperApplicationTicketEntity s JOIN FETCH s.ticket")
    Page<ShipperApplicationTicketEntity> findAllWithTicket(Pageable pageable);

    @Query("SELECT s FROM ShipperApplicationTicketEntity s JOIN FETCH s.ticket WHERE s.ticketId = :id")
    Optional<ShipperApplicationTicketEntity> findWithTicketById(UUID id);

    boolean existsByTicket_UserIdAndTicket_Status(UUID userId, TicketStatus status);
}
