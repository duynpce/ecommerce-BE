package org.example.productservice.infrastructure.web.data.adapter;

import lombok.RequiredArgsConstructor;
import org.example.productservice.application.repository.VoucherRepository;
import org.example.productservice.domain.model.Voucher;
import org.example.productservice.infrastructure.mapper.VoucherMapperMapstruct;
import org.example.productservice.infrastructure.web.data.entity.VoucherEntity;
import org.example.productservice.infrastructure.web.data.springdata.SpringDataVoucherRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VoucherRepositoryAdapter implements VoucherRepository {
    private final SpringDataVoucherRepository repository;
    private final VoucherMapperMapstruct mapper;

    @Override
    public Voucher save(Voucher voucher) {
        VoucherEntity entity = mapper.toEntity(voucher);
        if (entity.getId() == null) entity.setId(UUID.randomUUID());
        return mapper.toDomain(repository.save(entity));
    }

    @Override public Optional<Voucher> findById(UUID id) { return repository.findById(id).map(mapper::toDomain); }
    @Override public Optional<Voucher> findByCode(String code) { return repository.findByCodeIgnoreCase(code).map(mapper::toDomain); }
    @Override public boolean existsByCode(String code) { return repository.existsByCodeIgnoreCase(code); }
    @Override public List<Voucher> findAll() { return repository.findAll().stream().map(mapper::toDomain).toList(); }
    @Override public List<Voucher> findByShopIds(List<UUID> shopIds) { return repository.findByShopIdIn(shopIds).stream().map(mapper::toDomain).toList(); }
    @Override public void deleteById(UUID id) { repository.deleteById(id); }
}
