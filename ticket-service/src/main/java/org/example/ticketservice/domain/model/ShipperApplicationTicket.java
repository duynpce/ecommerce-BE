package org.example.ticketservice.domain.model;

import org.example.ticketservice.domain.constant.TicketType;
import org.example.ticketservice.domain.constant.VehicleType;

public class ShipperApplicationTicket extends Ticket {

    private String identityCardNumber;
    private String driverLicenseNumber;
    private VehicleType vehicleType;
    private String vehiclePlateNumber;
    private String phoneNumber;

    public ShipperApplicationTicket() {
        setType(TicketType.SHIPPER_APPLICATION);
    }

    public String getIdentityCardNumber() { return identityCardNumber; }
    public void setIdentityCardNumber(String value) { identityCardNumber = require(value, "Identity card number"); }

    public String getDriverLicenseNumber() { return driverLicenseNumber; }
    public void setDriverLicenseNumber(String value) { driverLicenseNumber = require(value, "Driver license number"); }

    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType value) {
        if (value == null) throw new IllegalArgumentException("Vehicle type cannot be null.");
        vehicleType = value;
    }

    public String getVehiclePlateNumber() { return vehiclePlateNumber; }
    public void setVehiclePlateNumber(String value) { vehiclePlateNumber = require(value, "Vehicle plate number"); }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String value) { phoneNumber = require(value, "Phone number"); }

    private String require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank.");
        }
        return value.trim();
    }
}
