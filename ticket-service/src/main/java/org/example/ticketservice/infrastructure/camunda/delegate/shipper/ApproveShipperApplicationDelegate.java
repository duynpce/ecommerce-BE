package org.example.ticketservice.infrastructure.camunda.delegate.shipper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.ticketservice.application.client.AuthClient;
import org.example.ticketservice.application.repository.ShipperApplicationTicketRepository;
import org.example.ticketservice.domain.model.ShipperApplicationTicket;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component("approveShipperApplicationDelegate")
@RequiredArgsConstructor
public class ApproveShipperApplicationDelegate implements JavaDelegate {
    private final ShipperApplicationTicketRepository repository;
    private final AuthClient authClient;

    @Override
    public void execute(DelegateExecution execution) {
        UUID ticketId = UUID.fromString(
                (String) execution.getVariable("shipperApplicationTicketId"));
        String userId = (String) execution.getVariable("userId");
        ShipperApplicationTicket ticket = repository.findById(ticketId);
        ticket.approve();
        repository.save(ticket);
        authClient.promoteAccountToShipper(userId);
        log.info("Shipper application approved: ticketId={}, userId={}", ticketId, userId);
    }
}
