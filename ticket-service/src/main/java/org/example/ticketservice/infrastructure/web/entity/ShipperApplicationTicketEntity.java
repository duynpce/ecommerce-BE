package org.example.ticketservice.infrastructure.web.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.ticketservice.domain.constant.VehicleType;

import java.util.UUID;

@Entity
@Table(name = "shipper_application_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipperApplicationTicketEntity {

    @Id
    @Column(name = "ticket_id", updatable = false, nullable = false)
    private UUID ticketId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", referencedColumnName = "id")
    private TicketEntity ticket;

    @Column(name = "identity_card_number", nullable = false, length = 50)
    private String identityCardNumber;

    @Column(name = "driver_license_number", nullable = false, length = 50)
    private String driverLicenseNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 30)
    private VehicleType vehicleType;

    @Column(name = "vehicle_plate_number", nullable = false, length = 30)
    private String vehiclePlateNumber;

    @Column(name = "phone_number", nullable = false, length = 25)
    private String phoneNumber;
}
