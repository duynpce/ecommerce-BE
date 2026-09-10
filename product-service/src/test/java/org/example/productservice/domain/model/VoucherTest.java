package org.example.productservice.domain.model;

import org.example.productservice.domain.constant.ProductCategory;
import org.example.productservice.domain.constant.VoucherApplicableCategory;
import org.example.productservice.domain.constant.VoucherDiscountType;
import org.example.productservice.domain.constant.VoucherType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VoucherTest {

    @Test
    void allCategoryNormalizesHashSetAndMatchesEveryProductCategory() {
        Voucher voucher = validVoucher();
        voucher.setApplicableCategories(Set.of(
                VoucherApplicableCategory.ALL,
                VoucherApplicableCategory.ELECTRONICS
        ));

        voucher.validate();

        assertEquals(Set.of(VoucherApplicableCategory.ALL), voucher.getApplicableCategories());
        assertTrue(voucher.appliesTo(ProductCategory.BOOKS));
    }

    @Test
    void percentageDiscountHonorsMaximumDiscount() {
        Voucher voucher = validVoucher();
        voucher.setDiscountType(VoucherDiscountType.PERCENTAGE);
        voucher.setDiscountValue(new BigDecimal("20"));
        voucher.setMaximumDiscount(new BigDecimal("50"));

        assertEquals(new BigDecimal("50.00"), voucher.calculateDiscount(
                new BigDecimal("500"), new BigDecimal("500")));
    }

    @Test
    void minimumSpendUsesTransactionSubtotalBeforeDiscount() {
        Voucher voucher = validVoucher();
        voucher.setMinimumSpend(new BigDecimal("1000"));

        assertEquals(new BigDecimal("20.00"), voucher.calculateDiscount(
                new BigDecimal("200"), new BigDecimal("1000")));
        IllegalStateException error = assertThrows(IllegalStateException.class, () ->
                voucher.calculateDiscount(new BigDecimal("200"), new BigDecimal("999.99")));
        assertTrue(error.getMessage().contains("before discount"));
    }

    @Test
    void transactionEmbedsVoucherSnapshotAndUsesItsDiscount() {
        Voucher voucher = validVoucher();
        VoucherSnapshot snapshot = VoucherSnapshot.of(voucher, new BigDecimal("15.00"));
        Transaction transaction = new Transaction();

        transaction.applyVoucher(snapshot);

        assertEquals(List.of(snapshot), transaction.getVouchers());
        assertEquals(new BigDecimal("15.00"), transaction.getDiscountAmount());
        assertEquals(voucher.getMinimumSpend(), snapshot.getMinimumSpend());
    }

    @Test
    void transactionAllowsOnlyOneVoucherOfEachType() {
        Voucher platform = validVoucher();
        Voucher shipping = validVoucher();
        shipping.setType(VoucherType.SHIPPING);
        VoucherSnapshot platformSnapshot = VoucherSnapshot.of(platform, new BigDecimal("20.00"));
        VoucherSnapshot shippingSnapshot = VoucherSnapshot.of(shipping, BigDecimal.ZERO);
        Transaction transaction = new Transaction();

        transaction.applyVouchers(List.of(platformSnapshot, shippingSnapshot));

        assertEquals(2, transaction.getVouchers().size());
        assertEquals(new BigDecimal("20.00"), transaction.getDiscountAmount());
        assertThrows(IllegalArgumentException.class, () -> transaction.applyVoucher(platformSnapshot));
    }

    private Voucher validVoucher() {
        Voucher voucher = new Voucher();
        voucher.setId(UUID.randomUUID());
        voucher.setCode("SAVE20");
        voucher.setName("Save twenty");
        voucher.setType(VoucherType.PLATFORM);
        voucher.setDiscountType(VoucherDiscountType.FIXED_AMOUNT);
        voucher.setDiscountValue(new BigDecimal("20"));
        voucher.setMinimumSpend(BigDecimal.ZERO);
        voucher.setStartsAt(Instant.now().minusSeconds(60));
        voucher.setEndsAt(Instant.now().plusSeconds(3600));
        voucher.setActive(true);
        voucher.setApplicableCategories(Set.of(VoucherApplicableCategory.ALL));
        voucher.validate();
        return voucher;
    }
}
