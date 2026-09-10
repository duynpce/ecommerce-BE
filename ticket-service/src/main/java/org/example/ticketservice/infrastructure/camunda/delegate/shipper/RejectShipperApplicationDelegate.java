package org.example.ticketservice.infrastructure.camunda.delegate.shipper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.repository.ShipperApplicationTicketRepository;
import org.example.ticketservice.domain.model.ShipperApplicationTicket;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component("rejectShipperApplicationDelegate")
@RequiredArgsConstructor
public class RejectShipperApplicationDelegate implements JavaDelegate {
    private final ShipperApplicationTicketRepository repository;

    @Override
    public void execute(DelegateExecution execution) {
        UUID ticketId = UUID.fromString(
                (String) execution.getVariable("shipperApplicationTicketId"));
        ShipperApplicationTicket ticket = repository.findById(ticketId);
        ticket.reject();
        repository.save(ticket);
        log.info("Shipper application rejected: ticketId={}", ticketId);
    }
}
