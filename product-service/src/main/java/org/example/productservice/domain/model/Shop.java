package org.example.productservice.domain.model;

import org.example.productservice.domain.constant.ShopStatus;
import org.example.productservice.domain.valueobject.Address;

import java.util.UUID;

public class Shop {
    private UUID id;
    private UUID contributorId;
    private String name;
    private String description;
    private String logoUrl;
    private Address pickUpAddress;
    private ShopStatus status;

    public Shop(UUID id, UUID contributorId, String name, String description,
                String logoUrl, Address pickUpAddress, ShopStatus status) {
        this.id = id;
        this.contributorId = contributorId;
        this.name = name;
        this.description = description;
        this.logoUrl = logoUrl;
        this.pickUpAddress = pickUpAddress;
        this.status = status;
    }

    public Shop() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getContributorId() {
        return contributorId;
    }

    public void setContributorId(UUID contributorId) {
        this.contributorId = contributorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Shop name cannot be null or empty");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Address getPickUpAddress() {
        return pickUpAddress;
    }

    public void setPickUpAddress(Address pickUpAddress) {
        this.pickUpAddress = pickUpAddress;
    }

    public ShopStatus getStatus() {
        return status;
    }

    public void setStatus(ShopStatus status) {
        this.status = status;
    }
}
