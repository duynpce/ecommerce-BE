package org.example.productservice.infrastructure.mapper;

import org.example.productservice.application.command.CreateShopCommand;
import org.example.productservice.application.command.UpdateShopCommand;
import org.example.productservice.application.criteria.ShopSearchCriteria;
import org.example.productservice.application.mapper.ShopMapper;
import org.example.productservice.domain.model.Shop;
import org.example.productservice.domain.valueobject.Address;
import org.example.productservice.infrastructure.web.data.entity.ShopEntity;
import org.example.productservice.infrastructure.web.dto.shop.CreateShopRequest;
import org.example.productservice.infrastructure.web.dto.shop.ShopFilter;
import org.example.productservice.infrastructure.web.dto.shop.ShopResponse;
import org.example.productservice.infrastructure.web.dto.shop.UpdateShopRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ShopMapperMapstruct extends ShopMapper {

    // ── ShopEntity → Shop (domain) ─────────────────────────────────────────
    @Mapping(target = "pickUpAddress", source = "pickUpAddress", qualifiedByName = "stringToAddress")
    Shop toDomain(ShopEntity entity);

    // ── CreateShopCommand → Shop (domain) ──────────────────────────────────
    // Address is already an Address object — direct mapping, no qualifier needed
    Shop toDomain(CreateShopCommand command);

    // ── Shop (domain) → ShopEntity ─────────────────────────────────────────
    @Mapping(target = "pickUpAddress", source = "pickUpAddress", qualifiedByName = "addressToString")
    ShopEntity toEntity(Shop shop);

    // ── UpdateShopCommand → Shop (in-place update) ─────────────────────────
    void updateFromCommand(UpdateShopCommand command, @MappingTarget Shop shop);

    // ── CreateShopRequest + contributorId → CreateShopCommand ──────────────
    // Flattened address fields → nested Address object
    @Mapping(target = "pickUpAddress", source = "request", qualifiedByName = "requestToAddress")
    @Mapping(target = "contributorId",  source = "contributorId")
    @Mapping(target = "logo",           source = "request.logo")
    @Mapping(target = "name",           source = "request.name")
    @Mapping(target = "description",    source = "request.description")
    CreateShopCommand toCommand(CreateShopRequest request, UUID contributorId);

    // ── UpdateShopRequest + id + senderId → UpdateShopCommand ──────────────
    @Mapping(target = "pickUpAddress", source = "request", qualifiedByName = "updateRequestToAddress")
    @Mapping(target = "id",          source = "id")
    @Mapping(target = "senderId",    source = "senderId")
    @Mapping(target = "logo",        source = "request.logo")
    @Mapping(target = "name",        source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "status",      source = "request.status")
    UpdateShopCommand toCommand(UpdateShopRequest request, UUID id, UUID senderId);

    // ── ShopFilter → ShopSearchCriteria ────────────────────────────────────
    ShopSearchCriteria toCriteria(ShopFilter filter);

    // ── Shop (domain) → ShopResponse ───────────────────────────────────────
    ShopResponse toResponse(Shop shop);


    // ── Named helpers ───────────────────────────────────────────────────────

    @Named("addressToString")
    default String addressToString(Address address) {
        return address != null ? address.toString() : null;
    }

    @Named("stringToAddress")
    default Address stringToAddress(String addressString) {
        return addressString != null ? new Address(addressString) : null;
    }

    /** Maps flattened fields from CreateShopRequest → Address */
    @Named("requestToAddress")
    default Address requestToAddress(CreateShopRequest req) {
        if (req == null) return null;
        if (req.street() == null && req.ward() == null && req.district() == null
                && req.city() == null && req.country() == null) return null;
        return new Address(req.street(), req.ward(), req.district(),
                req.city(), req.country(), req.zipCode());
    }

    /** Maps flattened fields from UpdateShopRequest → Address */
    @Named("updateRequestToAddress")
    default Address updateRequestToAddress(UpdateShopRequest req) {
        if (req == null) return null;
        if (req.street() == null && req.ward() == null && req.district() == null
                && req.city() == null && req.country() == null) return null;
        return new Address(req.street(), req.ward(), req.district(),
                req.city(), req.country(), req.zipCode());
    }
}