package org.example.productservice.application.service;

import lombok.RequiredArgsConstructor;
import org.example.productservice.application.command.SaveVoucherCommand;
import org.example.productservice.application.command.VoucherLine;
import org.example.productservice.application.repository.ShopRepository;
import org.example.productservice.application.repository.VoucherRepository;
import org.example.productservice.application.usecase.VoucherUseCase;
import org.example.productservice.domain.constant.VoucherType;
import org.example.productservice.domain.exception.ConflictException;
import org.example.productservice.domain.exception.ForbiddenException;
import org.example.productservice.domain.exception.InvalidStateException;
import org.example.productservice.domain.exception.NotFoundException;
import org.example.productservice.domain.model.Shop;
import org.example.productservice.domain.model.Voucher;
import org.example.productservice.domain.model.VoucherApplication;
import org.example.productservice.domain.model.VoucherSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class VoucherService implements VoucherUseCase {
    private final VoucherRepository voucherRepository;
    private final ShopRepository shopRepository;

    @Override
    @Transactional
    public Voucher create(SaveVoucherCommand command, UUID actorId, boolean admin) {
        String normalizedCode = normalizeCode(command.code());
        if (voucherRepository.existsByCode(normalizedCode)) {
            throw new ConflictException("Voucher code already exists: " + normalizedCode);
        }
        Voucher voucher = new Voucher();
        voucher.setId(UUID.randomUUID());
        applyCommand(voucher, command);
        requireCanManage(voucher, actorId, admin);
        voucher.setCreatedAt(Instant.now());
        voucher.validate();
        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public Voucher update(UUID id, SaveVoucherCommand command, UUID actorId, boolean admin) {
        Voucher voucher = findById(id);
        requireCanManage(voucher, actorId, admin);
        String normalizedCode = normalizeCode(command.code());
        voucherRepository.findByCode(normalizedCode)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new ConflictException("Voucher code already exists: " + normalizedCode); });
        applyCommand(voucher, command);
        requireCanManage(voucher, actorId, admin);
        voucher.validate();
        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional(readOnly = true)
    public Voucher findById(UUID id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Voucher not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Voucher> findManageable(UUID actorId, boolean admin) {
        if (admin) return voucherRepository.findAll();
        List<UUID> shopIds = shopRepository.findByContributorId(actorId).stream().map(Shop::getId).toList();
        return shopIds.isEmpty() ? List.of() : voucherRepository.findByShopIds(shopIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherApplication> findAvailable(List<VoucherLine> lines) {
        if (lines == null || lines.isEmpty()) throw new IllegalArgumentException("Voucher lookup requires items");
        return voucherRepository.findAll().stream()
                .map(voucher -> evaluate(voucher, lines))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .sorted(Comparator.comparing((VoucherApplication item) -> item.voucher().getType())
                        .thenComparing(VoucherApplication::discountAmount, Comparator.reverseOrder()))
                .toList();
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID actorId, boolean admin) {
        Voucher voucher = findById(id);
        requireCanManage(voucher, actorId, admin);
        voucherRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherApplication preview(String code, List<VoucherLine> lines) {
        if (lines == null || lines.isEmpty()) throw new IllegalArgumentException("Voucher preview requires items");
        Voucher voucher = voucherRepository.findByCode(normalizeCode(code))
                .orElseThrow(() -> new NotFoundException("Voucher code not found"));
        return requireApplication(voucher, lines);
    }

    private java.util.Optional<VoucherApplication> evaluate(Voucher voucher, List<VoucherLine> lines) {
        try {
            return java.util.Optional.of(requireApplication(voucher, lines));
        } catch (InvalidStateException | IllegalArgumentException ex) {
            return java.util.Optional.empty();
        }
    }

    private VoucherApplication requireApplication(Voucher voucher, List<VoucherLine> lines) {
        try {
            voucher.requireAvailable(Instant.now());
        } catch (IllegalStateException ex) {
            throw new InvalidStateException(ex.getMessage());
        }

        BigDecimal transactionSubtotal = lines.stream()
                .map(line -> line.product().getPrice().multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Shipping calculation will be added later. For now its eligible subtotal is deliberately zero,
        // so a shipping voucher is selectable but contributes min(calculated discount, 0) = 0.
        BigDecimal eligibleSubtotal = voucher.getType() == VoucherType.SHIPPING
                ? BigDecimal.ZERO
                : lines.stream()
                .filter(line -> voucher.getType() == VoucherType.PLATFORM
                        || voucher.getShopId().equals(line.product().getShopId()))
                .filter(line -> voucher.appliesTo(line.product().getCategory()))
                .map(line -> line.product().getPrice().multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (voucher.getType() != VoucherType.SHIPPING && eligibleSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidStateException("Voucher does not apply to any selected product");
        }
        BigDecimal discount;
        try {
            discount = voucher.calculateDiscount(eligibleSubtotal, transactionSubtotal);
        } catch (IllegalStateException ex) {
            throw new InvalidStateException(ex.getMessage());
        }
        return new VoucherApplication(voucher, transactionSubtotal, eligibleSubtotal, discount,
                VoucherSnapshot.of(voucher, discount));
    }

    @Override
    @Transactional
    public void consume(VoucherApplication application) {
        Voucher latest = voucherRepository.findById(application.voucher().getId())
                .orElseThrow(() -> new NotFoundException("Voucher no longer exists"));
        try {
            latest.requireAvailable(Instant.now());
        } catch (IllegalStateException ex) {
            throw new InvalidStateException(ex.getMessage());
        }
        latest.incrementUsedCount();
        voucherRepository.save(latest);
    }

    private void applyCommand(Voucher voucher, SaveVoucherCommand command) {
        voucher.setCode(normalizeCode(command.code()));
        voucher.setName(command.name());
        voucher.setDescription(command.description());
        voucher.setType(command.type());
        voucher.setShopId(command.shopId());
        voucher.setDiscountType(command.discountType());
        voucher.setDiscountValue(command.discountValue());
        voucher.setMinimumSpend(command.minimumSpend());
        voucher.setMaximumDiscount(command.maximumDiscount());
        voucher.setStartsAt(command.startsAt());
        voucher.setEndsAt(command.endsAt());
        voucher.setUsageLimit(command.usageLimit());
        voucher.setActive(command.active());
        voucher.setApplicableCategories(command.applicableCategories());
    }

    private void requireCanManage(Voucher voucher, UUID actorId, boolean admin) {
        if (voucher.getType() != VoucherType.SHOP) {
            if (!admin) throw new ForbiddenException("Only admins can manage platform and shipping vouchers");
            return;
        }
        Shop shop = shopRepository.findById(voucher.getShopId())
                .orElseThrow(() -> new NotFoundException("Shop not found: " + voucher.getShopId()));
        if (!admin && !actorId.equals(shop.getContributorId())) {
            throw new ForbiddenException("You can only manage vouchers for your own shop");
        }
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("Voucher code is required");
        return code.trim().toUpperCase();
    }
}
