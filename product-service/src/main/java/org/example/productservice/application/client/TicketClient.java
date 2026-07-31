package org.example.productservice.application.client;

import org.example.productservice.infrastructure.ticket.dto.StartBuyingProcedureRequest;

public interface TicketClient {
    void startBuyingProcedure(StartBuyingProcedureRequest request);
}
