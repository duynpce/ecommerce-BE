package org.example.productservice.domain.constant;

public enum VoucherApplicableCategory {
    ALL,
    ELECTRONICS,
    CLOTHING,
    BOOKS,
    HOME_AND_KITCHEN,
    BEAUTY_AND_HEALTH,
    MEDICALS,
    ELSE;

    public boolean matches(ProductCategory category) {
        return this == ALL || (category != null && name().equals(category.name()));
    }
}
