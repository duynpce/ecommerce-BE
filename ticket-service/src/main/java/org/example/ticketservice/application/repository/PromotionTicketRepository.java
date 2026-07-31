package org.example.ticketservice.application.repository;

import org.example.ticketservice.domain.model.PromotionTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PromotionTicketRepository {

    PromotionTicket save(PromotionTicket domain);

    PromotionTicket findById(UUID id);

    Page<PromotionTicket> findAll(Pageable pageable);

    boolean existsByIdentityCardNumber(String identityCardNumber);

    boolean existsByBankAccountNumber(String bankAccountNumber);

    boolean existsByTaxId(String taxId);
}
