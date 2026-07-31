package org.example.productservice.infrastructure.ticket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.productservice.application.client.TicketClient;
import org.example.productservice.infrastructure.ticket.dto.StartBuyingProcedureRequest;
import org.example.productservice.infrastructure.ticket.httpclient.TicketHttpClient;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class TicketClientAdapter implements TicketClient {
    private final TicketHttpClient ticketHttpClient;

    @Override
    public void startBuyingProcedure(StartBuyingProcedureRequest request) {
        log.info("[ticket-client-adapter] start buying procedure with request: {}", request);
        ticketHttpClient.startBuyingProcedure(request);
    }
}
