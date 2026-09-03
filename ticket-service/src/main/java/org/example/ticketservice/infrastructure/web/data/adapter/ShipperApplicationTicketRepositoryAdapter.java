package org.example.ticketservice.infrastructure.web.data.adapter;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.example.ticketservice.application.repository.ShipperApplicationTicketRepository;
import org.example.ticketservice.domain.constant.TicketStatus;
import org.example.ticketservice.domain.constant.TicketType;
import org.example.ticketservice.domain.model.ShipperApplicationTicket;
import org.example.ticketservice.infrastructure.web.data.springdata.SpringDataShipperApplicationTicketRepository;
import org.example.ticketservice.infrastructure.web.data.springdata.SpringDataTicketRepository;
import org.example.ticketservice.infrastructure.web.entity.ShipperApplicationTicketEntity;
import org.example.ticketservice.infrastructure.web.entity.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ShipperApplicationTicketRepositoryAdapter
        implements ShipperApplicationTicketRepository {

    private final SpringDataTicketRepository ticketRepository;
    private final SpringDataShipperApplicationTicketRepository shipperRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public ShipperApplicationTicket save(ShipperApplicationTicket domain) {
        boolean isNew = domain.getId() == null;
        TicketEntity ticket = isNew
                ? new TicketEntity()
                : ticketRepository.findById(domain.getId()).orElseThrow();
        ticket.setType(TicketType.SHIPPER_APPLICATION);
        ticket.setStatus(domain.getStatus());
        ticket.setCreatedAt(domain.getCreatedAt());
        ticket.setUserId(domain.getUserId());
        TicketEntity savedTicket = ticketRepository.save(ticket);

        ShipperApplicationTicketEntity entity;
        if (isNew) {
            entity = ShipperApplicationTicketEntity.builder()
                    .ticketId(savedTicket.getId())
                    .ticket(savedTicket)
                    .build();
            copyApplicationFields(domain, entity);

            // The child uses the ticket UUID as an assigned @MapsId value. Calling
            // JpaRepository.save here would select merge because the ID is non-null,
            // which Hibernate 6 treats as an update of a missing row. Persist makes
            // the intended INSERT explicit.
            entityManager.persist(entity);
        } else {
            entity = shipperRepository.findWithTicketById(savedTicket.getId())
                    .orElseThrow(() -> new org.example.ticketservice.domain.exception.NotFoundException(
                            "Shipper application not found: " + savedTicket.getId()));
            copyApplicationFields(domain, entity);
        }

        entityManager.flush();
        return toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipperApplicationTicket findById(UUID id) {
        return shipperRepository.findWithTicketById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new org.example.ticketservice.domain.exception.NotFoundException(
                        "Shipper application not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipperApplicationTicket> findAll(Pageable pageable) {
        return shipperRepository.findAllWithTicket(pageable).map(this::toDomain);
    }

    @Override
    public boolean existsPendingByUserId(UUID userId) {
        return shipperRepository.existsByTicket_UserIdAndTicket_Status(
                userId, TicketStatus.PENDING);
    }

    private ShipperApplicationTicket toDomain(ShipperApplicationTicketEntity entity) {
        TicketEntity ticket = entity.getTicket();
        ShipperApplicationTicket domain = new ShipperApplicationTicket();
        domain.setId(ticket.getId());
        domain.setType(ticket.getType());
        domain.setStatus(ticket.getStatus());
        domain.setCreatedAt(ticket.getCreatedAt());
        domain.setUserId(ticket.getUserId());
        domain.setIdentityCardNumber(entity.getIdentityCardNumber());
        domain.setDriverLicenseNumber(entity.getDriverLicenseNumber());
        domain.setVehicleType(entity.getVehicleType());
        domain.setVehiclePlateNumber(entity.getVehiclePlateNumber());
        domain.setPhoneNumber(entity.getPhoneNumber());
        return domain;
    }

    private void copyApplicationFields(
            ShipperApplicationTicket source,
            ShipperApplicationTicketEntity target) {
        target.setIdentityCardNumber(source.getIdentityCardNumber());
        target.setDriverLicenseNumber(source.getDriverLicenseNumber());
        target.setVehicleType(source.getVehicleType());
        target.setVehiclePlateNumber(source.getVehiclePlateNumber());
        target.setPhoneNumber(source.getPhoneNumber());
    }
}
