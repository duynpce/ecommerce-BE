package org.example.ticketservice.infrastructure.web.data.springdata;

import org.example.ticketservice.infrastructure.web.entity.PromotionTicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataPromotionTicketRepository extends JpaRepository<PromotionTicketEntity, UUID> {

    @Query("SELECT p FROM PromotionTicketEntity p JOIN FETCH p.ticket")
    Page<PromotionTicketEntity> findAllWithTicket(Pageable pageable);

    @Query("SELECT p FROM PromotionTicketEntity p JOIN FETCH p.ticket WHERE p.ticketId = :id")
    Optional<PromotionTicketEntity> findWithTicketById(UUID id);

    boolean existsByIdentityCardNumber(String identityCardNumber);

    boolean existsByBankAccountNumber(String bankAccountNumber);

    boolean existsByTaxId(String taxId);
}
