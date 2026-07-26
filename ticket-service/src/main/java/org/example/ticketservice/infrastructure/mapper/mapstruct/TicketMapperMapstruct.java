package org.example.ticketservice.infrastructure.mapper.mapstruct;

import org.example.ticketservice.application.command.SavePromotionRequestCommand;
import org.example.ticketservice.domain.model.PromotionTicket;
import org.example.ticketservice.infrastructure.web.dto.PromotionTicketResponse;
import org.example.ticketservice.infrastructure.web.dto.SavePromotionRequest;
import org.example.ticketservice.infrastructure.web.entity.PromotionTicketEntity;
import org.example.ticketservice.infrastructure.web.entity.TicketEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TicketMapperMapstruct {

    // PromotionTicket domain → TicketEntity (base fields only)
    TicketEntity toTicketEntity(PromotionTicket domain);

    // PromotionTicket domain + saved TicketEntity → PromotionTicketEntity
    @Mapping(target = "ticket",             source = "ticketEntity")
    @Mapping(target = "identityCardNumber", source = "domain.identityCardNumber")
    @Mapping(target = "bankName",           source = "domain.bankName")
    @Mapping(target = "bankAccountNumber",  source = "domain.bankAccountNumber")
    @Mapping(target = "taxId",              source = "domain.taxId")
    PromotionTicketEntity toPromotionTicketEntity(PromotionTicket domain, TicketEntity ticketEntity);

    // PromotionTicketEntity → PromotionTicket domain
    @Mapping(target = "id",                 source = "ticket.id")
    @Mapping(target = "type",               source = "ticket.type")
    @Mapping(target = "status",             source = "ticket.status")
    @Mapping(target = "createdAt",          source = "ticket.createdAt")
    @Mapping(target = "userId",             source = "ticket.userId")
    PromotionTicket toDomain(PromotionTicketEntity entity);

    PromotionTicket toDomain(SavePromotionRequestCommand command);

    // SavePromotionRequest DTO + userId from token → SavePromotionRequestCommand
    @Mapping(target = "identityCardNumber", source = "request.identityCardNumber")
    @Mapping(target = "bankName",           source = "request.bankName")
    @Mapping(target = "bankAccountNumber",  source = "request.bankAccountNumber")
    @Mapping(target = "taxId",              source = "request.taxId")
    SavePromotionRequestCommand toCommand(SavePromotionRequest request, UUID userId);

    PromotionTicketResponse toResponse(PromotionTicket promotionTicket);
}
