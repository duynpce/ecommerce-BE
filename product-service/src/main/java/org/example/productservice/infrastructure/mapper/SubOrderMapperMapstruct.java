package org.example.productservice.infrastructure.mapper;

import org.example.productservice.application.command.CreateSubOrderCommand;
import org.example.productservice.application.command.UpdateSubOrderCommand;
import org.example.productservice.application.criteria.SubOrderSearchCriteria;
import org.example.productservice.application.mapper.SubOrderMapper;
import org.example.productservice.domain.model.ProductSnapshot;
import org.example.productservice.domain.model.SubOrder;
import org.example.productservice.infrastructure.web.data.entity.ProductSnapshotEntity;
import org.example.productservice.infrastructure.web.data.entity.SubOrderEntity;
import org.example.productservice.infrastructure.web.dto.suborder.CreateSubOrderRequest;
import org.example.productservice.infrastructure.web.dto.suborder.ProductSnapshotResponse;
import org.example.productservice.infrastructure.web.dto.suborder.SubOrderFilter;
import org.example.productservice.infrastructure.web.dto.suborder.SubOrderResponse;
import org.example.productservice.infrastructure.web.dto.suborder.UpdateSubOrderRequest;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface SubOrderMapperMapstruct extends SubOrderMapper {

    @Override
    SubOrder toDomain(SubOrderEntity entity);

    @Override
    @Mapping(target = "items", ignore = true)
    SubOrder toDomain(CreateSubOrderCommand command);

    @Override
    SubOrderEntity toEntity(SubOrder subOrder);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromCommand(UpdateSubOrderCommand command, @MappingTarget SubOrder subOrder);

    @Override
    CreateSubOrderCommand toCommand(CreateSubOrderRequest request, UUID customerId);

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "status", ignore = true)
    UpdateSubOrderCommand toCommand(UpdateSubOrderRequest request, UUID id);

    @Override
    @Mapping(target = "customerId",    source = "customerId")
    @Mapping(target = "shopId",        source = "filter.shopId")
    @Mapping(target = "transactionId", source = "filter.transactionId")
    @Mapping(target = "createdFrom",   source = "filter.createdFrom", qualifiedByName = "localDateToInstantStart")
    @Mapping(target = "createdTo",     source = "filter.createdTo",   qualifiedByName = "localDateToInstantEnd")
    SubOrderSearchCriteria toCriteria(SubOrderFilter filter, UUID customerId);

    @Override
    @Mapping(target = "customerId",    ignore = true)
    @Mapping(target = "shopId",        source = "shopId")
    @Mapping(target = "transactionId", source = "filter.transactionId")
    @Mapping(target = "createdFrom",   source = "filter.createdFrom", qualifiedByName = "localDateToInstantStart")
    @Mapping(target = "createdTo",     source = "filter.createdTo",   qualifiedByName = "localDateToInstantEnd")
    SubOrderSearchCriteria toShopCriteria(SubOrderFilter filter, UUID shopId);

    @Override
    SubOrderResponse toResponse(SubOrder subOrder);

    @Override
    @Mapping(target = "subtotal", expression = "java(snapshot.subtotal())")
    ProductSnapshotResponse toSnapshotResponse(ProductSnapshot snapshot);

    @Override
    ProductSnapshot toDomain(ProductSnapshotEntity entity);

    @Override
    ProductSnapshotEntity toEntity(ProductSnapshot snapshot);
}
