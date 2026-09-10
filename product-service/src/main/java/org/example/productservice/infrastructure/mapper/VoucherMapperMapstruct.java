package org.example.productservice.infrastructure.mapper;

import org.example.productservice.domain.model.Voucher;
import org.example.productservice.domain.model.VoucherSnapshot;
import org.example.productservice.infrastructure.web.data.entity.VoucherEntity;
import org.example.productservice.infrastructure.web.data.entity.VoucherSnapshotEntity;
import org.example.productservice.infrastructure.web.dto.voucher.VoucherSnapshotResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VoucherMapperMapstruct {
    Voucher toDomain(VoucherEntity entity);
    VoucherEntity toEntity(Voucher domain);
    VoucherSnapshot toDomain(VoucherSnapshotEntity entity);
    VoucherSnapshotEntity toEntity(VoucherSnapshot domain);
    VoucherSnapshotResponse toResponse(VoucherSnapshot domain);
}
