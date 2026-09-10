package org.example.productservice.application.service;

import org.example.productservice.application.command.SaveVoucherCommand;
import org.example.productservice.application.command.VoucherLine;
import org.example.productservice.application.repository.ShopRepository;
import org.example.productservice.application.repository.VoucherRepository;
import org.example.productservice.domain.constant.*;
import org.example.productservice.domain.exception.ForbiddenException;
import org.example.productservice.domain.exception.InvalidStateException;
import org.example.productservice.domain.model.Product;
import org.example.productservice.domain.model.Voucher;
import org.example.productservice.domain.model.VoucherApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class VoucherServiceTest {
    @Mock private VoucherRepository voucherRepository;
    @Mock private ShopRepository shopRepository;
    private VoucherService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new VoucherService(voucherRepository, shopRepository);
    }

    @Test
    void shopVoucherDiscountsOnlyMatchingShopAndCategories() {
        UUID shopId = UUID.randomUUID();
        Voucher voucher = voucher(VoucherType.SHOP, shopId, Set.of(VoucherApplicableCategory.BOOKS));
        when(voucherRepository.findByCode("BOOK30")).thenReturn(Optional.of(voucher));

        Product eligible = product(shopId, ProductCategory.BOOKS, "100");
        Product wrongCategory = product(shopId, ProductCategory.ELECTRONICS, "100");
        Product wrongShop = product(UUID.randomUUID(), ProductCategory.BOOKS, "100");

        VoucherApplication result = service.preview("book30", List.of(
                new VoucherLine(eligible, 2),
                new VoucherLine(wrongCategory, 3),
                new VoucherLine(wrongShop, 4)
        ));

        assertEquals(new BigDecimal("200"), result.eligibleSubtotal());
        assertEquals(new BigDecimal("30.00"), result.discountAmount());
        assertEquals(voucher.getId(), result.snapshot().getVoucherId());
    }

    @Test
    void contributorCannotCreatePlatformVoucher() {
        SaveVoucherCommand command = command(VoucherType.PLATFORM, null);
        when(voucherRepository.existsByCode("BOOK30")).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> service.create(command, UUID.randomUUID(), false));
    }

    @Test
    void previewChecksFullTransactionSubtotalBeforeCalculatingEligibleDiscount() {
        UUID shopId = UUID.randomUUID();
        Voucher voucher = voucher(VoucherType.SHOP, shopId, Set.of(VoucherApplicableCategory.BOOKS));
        voucher.setMinimumSpend(new BigDecimal("1000"));
        when(voucherRepository.findByCode("BOOK30")).thenReturn(Optional.of(voucher));

        Product eligibleBook = product(shopId, ProductCategory.BOOKS, "200");
        Product otherCategory = product(shopId, ProductCategory.ELECTRONICS, "800");

        VoucherApplication result = service.preview("BOOK30", List.of(
                new VoucherLine(eligibleBook, 1),
                new VoucherLine(otherCategory, 1)
        ));

        assertEquals(new BigDecimal("1000"), result.transactionSubtotal());
        assertEquals(new BigDecimal("200"), result.eligibleSubtotal());
        assertEquals(new BigDecimal("30.00"), result.discountAmount());

        assertThrows(InvalidStateException.class, () -> service.preview("BOOK30", List.of(
                new VoucherLine(eligibleBook, 1),
                new VoucherLine(product(shopId, ProductCategory.ELECTRONICS, "799.99"), 1)
        )));
    }

    @Test
    void shippingVoucherIsAvailableWithZeroDiscountUntilShippingLogicIsAdded() {
        Voucher voucher = voucher(VoucherType.SHIPPING, null, Set.of(VoucherApplicableCategory.ALL));
        when(voucherRepository.findByCode("BOOK30")).thenReturn(Optional.of(voucher));

        VoucherApplication result = service.preview("BOOK30", List.of(
                new VoucherLine(product(UUID.randomUUID(), ProductCategory.BOOKS, "1200"), 1)
        ));

        assertEquals(BigDecimal.ZERO, result.eligibleSubtotal());
        assertEquals(new BigDecimal("0.00"), result.discountAmount());
    }

    private Voucher voucher(VoucherType type, UUID shopId, Set<VoucherApplicableCategory> categories) {
        Voucher voucher = new Voucher();
        voucher.setId(UUID.randomUUID());
        voucher.setCode("BOOK30");
        voucher.setName("Book discount");
        voucher.setType(type);
        voucher.setShopId(shopId);
        voucher.setDiscountType(VoucherDiscountType.FIXED_AMOUNT);
        voucher.setDiscountValue(new BigDecimal("30"));
        voucher.setMinimumSpend(BigDecimal.ZERO);
        voucher.setStartsAt(Instant.now().minusSeconds(60));
        voucher.setEndsAt(Instant.now().plusSeconds(3600));
        voucher.setActive(true);
        voucher.setApplicableCategories(categories);
        voucher.validate();
        return voucher;
    }

    private Product product(UUID shopId, ProductCategory category, String price) {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setShopId(shopId);
        product.setCategory(category);
        product.setPrice(new BigDecimal(price));
        return product;
    }

    private SaveVoucherCommand command(VoucherType type, UUID shopId) {
        return new SaveVoucherCommand("BOOK30", "Book discount", null, type, shopId,
                VoucherDiscountType.FIXED_AMOUNT, new BigDecimal("30"), BigDecimal.ZERO, null,
                Instant.now().minusSeconds(60), Instant.now().plusSeconds(3600), null, true,
                Set.of(VoucherApplicableCategory.ALL));
    }
}
