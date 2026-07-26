package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.example.ticketservice.application.command.SavePromotionRequestCommand;
import org.example.ticketservice.application.mapper.TicketMapper;
import org.example.ticketservice.application.repository.PromotionTicketRepository;
import org.example.ticketservice.application.usecase.SavePromotionRequestUseCase;
import org.example.ticketservice.domain.model.PromotionTicket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavePromotionRequestService implements SavePromotionRequestUseCase {

    private static final String PROCESS_KEY = "promote-account-procedure";

    private final RuntimeService            runtimeService;
    private final PromotionTicketRepository promotionTicketRepository;
    private final TicketMapper              ticketMapper;

    @Override
    @Transactional
    public void execute(SavePromotionRequestCommand command) {
        validateUniqueness(command);

        PromotionTicket domain = ticketMapper.toDomain(command);

        PromotionTicket saved = promotionTicketRepository.save(domain);

        Map<String, Object> variables = new HashMap<>();
        variables.put("userId",             command.userId().toString());
        variables.put("promotionTicketId",  saved.getTicketId().toString());
        variables.put("identityCardNumber", command.identityCardNumber());
        variables.put("bankName",           command.bankName());
        variables.put("bankAccountNumber",  command.bankAccountNumber());
        variables.put("taxId",              command.taxId());

        runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables);
        log.info("Started Camunda process '{}' for promotion ticket ID: {}", PROCESS_KEY, saved.getTicketId());
    }

    private void validateUniqueness(SavePromotionRequestCommand command) {
        if (promotionTicketRepository.existsByIdentityCardNumber(command.identityCardNumber())) {
            throw new IllegalArgumentException("Identity card number is already registered.");
        }
        if (promotionTicketRepository.existsByBankAccountNumber(command.bankAccountNumber())) {
            throw new IllegalArgumentException("Bank account number is already registered.");
        }
        if (promotionTicketRepository.existsByTaxId(command.taxId())) {
            throw new IllegalArgumentException("Tax ID is already registered.");
        }
    }
}