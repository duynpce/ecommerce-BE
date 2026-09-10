package org.example.productservice.infrastructure.web.data.springdata;

import org.example.productservice.infrastructure.web.data.entity.VoucherEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataVoucherRepository extends MongoRepository<VoucherEntity, UUID> {
    Optional<VoucherEntity> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    List<VoucherEntity> findByShopIdIn(List<UUID> shopIds);
}
