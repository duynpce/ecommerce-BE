package org.example.ticketservice.infrastructure.web.data.adapter;

import lombok.RequiredArgsConstructor;
import org.example.ticketservice.application.mapper.TicketMapper;
import org.example.ticketservice.application.repository.PromotionTicketRepository;
import org.example.ticketservice.domain.constant.TicketType;
import org.example.ticketservice.domain.model.PromotionTicket;
import org.example.ticketservice.infrastructure.web.data.springdata.SpringDataPromotionTicketRepository;
import org.example.ticketservice.infrastructure.web.data.springdata.SpringDataTicketRepository;
import org.example.ticketservice.infrastructure.web.entity.PromotionTicketEntity;
import org.example.ticketservice.infrastructure.web.entity.TicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PromotionTicketRepositoryAdapter implements PromotionTicketRepository {

    private final SpringDataTicketRepository           ticketRepo;
    private final SpringDataPromotionTicketRepository  promotionTicketRepo;
    private final TicketMapper                         mapper;

    @Override
    @Transactional
    public PromotionTicket save(PromotionTicket domain) {
        TicketEntity ticketEntity = mapper.toEntity(domain);
        ticketEntity.setType(TicketType.PROMOTION);
        TicketEntity savedTicket = ticketRepo.save(ticketEntity);

        PromotionTicketEntity promotionEntity = mapper.toPromotionEntity(domain, savedTicket);
        PromotionTicketEntity savedPromotion  = promotionTicketRepo.save(promotionEntity);

        return mapper.toDomain(savedPromotion);
    }

    @Override
    public PromotionTicket findById(UUID id) {
        return promotionTicketRepo.findWithTicketById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new org.example.ticketservice.domain.exception.NotFoundException(
                        "Promotion ticket not found for id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromotionTicket> findAll(Pageable pageable) {
        return promotionTicketRepo.findAllWithTicket(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByIdentityCardNumber(String identityCardNumber) {
        return promotionTicketRepo.existsByIdentityCardNumber(identityCardNumber);
    }

    @Override
    public boolean existsByBankAccountNumber(String bankAccountNumber) {
        return promotionTicketRepo.existsByBankAccountNumber(bankAccountNumber);
    }

       @Override
    public boolean existsByTaxId(String taxId) {
        return promotionTicketRepo.existsByTaxId(taxId);
    }
}
