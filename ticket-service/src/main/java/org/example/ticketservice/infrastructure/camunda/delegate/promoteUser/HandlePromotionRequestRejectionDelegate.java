package org.example.ticketservice.infrastructure.camunda.delegate.promoteUser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.repository.PromotionTicketRepository;
import org.example.ticketservice.domain.model.PromotionTicket;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component("handlePromotionRequestRejectionDelegate")
@RequiredArgsConstructor
public class HandlePromotionRequestRejectionDelegate implements JavaDelegate {

    private final PromotionTicketRepository promotionTicketRepository;

    @Override
    public void execute(DelegateExecution execution) {

        UUID promotionTicketId = UUID.fromString((String) execution.getVariable("promotionTicketId"));

        PromotionTicket ticket = promotionTicketRepository.findById(promotionTicketId);
        ticket.reject();
        promotionTicketRepository.save(ticket);

        log.info("Promotion ticket rejected: id={}", promotionTicketId);
    }
}
