package org.example.productservice.application.mapper;

import org.example.productservice.application.command.CreateSubOrderCommand;
import org.example.productservice.application.command.UpdateSubOrderCommand;
import org.example.productservice.application.criteria.SubOrderSearchCriteria;
import org.example.productservice.domain.model.ProductSnapshot;
import org.example.productservice.domain.model.SubOrder;
import org.example.productservice.infrastructure.web.data.entity.ProductSnapshotEntity;
import org.example.productservice.infrastructure.web.data.entity.SubOrderEntity;
import org.example.productservice.infrastructure.web.dto.suborder.CreateSubOrderRequest;
import org.example.productservice.infrastructure.web.dto.suborder.ProductSnapshotResponse;
import org.example.productservice.infrastructure.web.dto.suborder.SubOrderFilter;
import org.example.productservice.infrastructure.web.dto.suborder.SubOrderResponse;
import org.example.productservice.infrastructure.web.dto.suborder.UpdateSubOrderRequest;

import java.util.UUID;

public interface SubOrderMapper {
    SubOrder toDomain(SubOrderEntity entity);
    SubOrder toDomain(CreateSubOrderCommand command);
    SubOrderEntity toEntity(SubOrder subOrder);
    void updateFromCommand(UpdateSubOrderCommand command, SubOrder subOrder);

    CreateSubOrderCommand toCommand(CreateSubOrderRequest request, UUID customerId);
    UpdateSubOrderCommand toCommand(UpdateSubOrderRequest request, UUID id);

    SubOrderSearchCriteria toCriteria(SubOrderFilter filter, UUID customerId);
    SubOrderSearchCriteria toShopCriteria(SubOrderFilter filter, UUID shopId);

    SubOrderResponse toResponse(SubOrder subOrder);
    ProductSnapshotResponse toSnapshotResponse(ProductSnapshot snapshot);

    ProductSnapshot toDomain(ProductSnapshotEntity entity);
    ProductSnapshotEntity toEntity(ProductSnapshot snapshot);
}
