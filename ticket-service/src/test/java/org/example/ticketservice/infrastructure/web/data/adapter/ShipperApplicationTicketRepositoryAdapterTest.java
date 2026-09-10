package org.example.ticketservice.infrastructure.web.data.adapter;

import org.example.ticketservice.application.repository.ShipperApplicationTicketRepository;
import org.example.ticketservice.domain.constant.TicketStatus;
import org.example.ticketservice.domain.constant.VehicleType;
import org.example.ticketservice.domain.model.ShipperApplicationTicket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
class ShipperApplicationTicketRepositoryAdapterTest {

    @Autowired
    private ShipperApplicationTicketRepository repository;

    @Test
    void insertsNewApplicationThenUpdatesItsStatus() {
        ShipperApplicationTicket application = new ShipperApplicationTicket();
        application.setUserId(UUID.randomUUID());
        application.setIdentityCardNumber("ID-123456");
        application.setDriverLicenseNumber("DL-654321");
        application.setVehicleType(VehicleType.MOTORBIKE);
        application.setVehiclePlateNumber("59A1-12345");
        application.setPhoneNumber("0901234567");

        ShipperApplicationTicket inserted = repository.save(application);

        assertNotNull(inserted.getId());
        assertEquals(TicketStatus.PENDING, inserted.getStatus());
        assertEquals(inserted.getId(), repository.findById(inserted.getId()).getId());

        inserted.approve();
        ShipperApplicationTicket updated = repository.save(inserted);

        assertEquals(TicketStatus.APPROVED, updated.getStatus());
        assertEquals(TicketStatus.APPROVED, repository.findById(inserted.getId()).getStatus());
    }
}
