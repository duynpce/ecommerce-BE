package org.example.ticketservice.infrastructure.camunda.delegate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.AuthClient;
import org.example.ticketservice.application.client.UserClient;
import org.example.ticketservice.application.repository.PromotionTicketRepository;
import org.example.ticketservice.domain.model.PromotionTicket;
import org.example.ticketservice.infrastructure.web.dto.CreateContributorProfileRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component("handlePromotionRequestApprovalDelegate")
@RequiredArgsConstructor
public class HandlePromotionRequestApprovalDelegate implements JavaDelegate {

    private final PromotionTicketRepository promotionTicketRepository;
    private final AuthClient authClient;
    private final UserClient userClient;

    @Override
    public void execute(DelegateExecution execution) {

        UUID promotionTicketId = UUID.fromString((String) execution.getVariable("promotionTicketId"));
        String userId  = (String) execution.getVariable("userId");

        PromotionTicket ticket = promotionTicketRepository.findById(promotionTicketId);
        ticket.approve();
        promotionTicketRepository.save(ticket);

        authClient.promoteAccountToContributor(userId);
        userClient.createContributorProfile(CreateContributorProfileRequest.builder()
                .accountId(UUID.fromString(userId))
                .identityCardNumber(ticket.getIdentityCardNumber())
                .bankAccountNumber(ticket.getBankAccountNumber())
                .bankName(ticket.getBankName())
                .taxId(ticket.getTaxId())
                .build());

        log.info("Promotion ticket approved: id={}, userId={}", promotionTicketId, userId);
    }
}
