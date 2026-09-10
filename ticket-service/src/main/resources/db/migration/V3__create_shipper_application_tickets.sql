CREATE TABLE shipper_application_tickets
(
    ticket_id              UUID         NOT NULL,
    identity_card_number   VARCHAR(50)  NOT NULL,
    driver_license_number  VARCHAR(50)  NOT NULL,
    vehicle_type           VARCHAR(30)  NOT NULL,
    vehicle_plate_number   VARCHAR(30)  NOT NULL,
    phone_number           VARCHAR(25)  NOT NULL,

    CONSTRAINT pk_shipper_application_tickets PRIMARY KEY (ticket_id),
    CONSTRAINT fk_shipper_application_ticket
        FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE CASCADE
);

CREATE INDEX idx_shipper_application_license
    ON shipper_application_tickets (driver_license_number);
CREATE INDEX idx_shipper_application_plate
    ON shipper_application_tickets (vehicle_plate_number);
