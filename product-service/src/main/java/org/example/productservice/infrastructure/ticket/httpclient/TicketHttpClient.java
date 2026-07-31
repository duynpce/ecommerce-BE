package org.example.productservice.infrastructure.ticket.httpclient;

import jakarta.validation.Valid;
import org.example.productservice.infrastructure.ticket.dto.StartBuyingProcedureRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface TicketHttpClient {

    @PostExchange("api/v1/tickets/transaction-tickets/start")
    void startBuyingProcedure(@RequestBody @Valid StartBuyingProcedureRequest request);

}
