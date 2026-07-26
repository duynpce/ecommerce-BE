package org.example.userservice.domain.model;

import org.example.userservice.domain.constant.Gender;
import org.example.userservice.domain.valueobject.PhoneNumber;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class AccountProfile {
    private UUID id;
    private String firstName;
    private String lastName;
    private PhoneNumber phoneNumber;
    private String address;
    private Gender gender;
    private Instant createdAt;
    private Instant updatedAt;

    public AccountProfile(String firstName, String lastName) {
        this.firstName = Objects.requireNonNull(firstName, "First name cannot be null");
        this.lastName  = Objects.requireNonNull(lastName,  "Last name cannot be null");
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public PhoneNumber getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(PhoneNumber phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
