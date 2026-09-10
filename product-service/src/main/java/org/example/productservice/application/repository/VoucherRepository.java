package org.example.productservice.application.repository;

import org.example.productservice.domain.model.Voucher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoucherRepository {
    Voucher save(Voucher voucher);
    Optional<Voucher> findById(UUID id);
    Optional<Voucher> findByCode(String code);
    boolean existsByCode(String code);
    List<Voucher> findAll();
    List<Voucher> findByShopIds(List<UUID> shopIds);
    void deleteById(UUID id);
}
