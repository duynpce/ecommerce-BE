package org.example.productservice.domain.valueobject;

public class Address {

    private String street;
    private String ward;
    private String district;
    private String city;
    private String country;

    public Address(String street, String ward, String district,
                   String city, String country, String zipCode) {
        this.street = street;
        this.ward = ward;
        this.district = district;
        this.city = city;
        this.country = country;
    }

    public Address(String fullAddress) {
        if (fullAddress != null && !fullAddress.isBlank()) {
            String[] parts = fullAddress.split(",\\s*");
            if (parts.length > 0) this.street = parts[0];
            if (parts.length > 1) this.ward = parts[1];
            if (parts.length > 2) this.district = parts[2];
            if (parts.length > 3) this.city = parts[3];
            if (parts.length > 4) this.country = parts[4];
        }
    }

    public Address() {
    }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

}
