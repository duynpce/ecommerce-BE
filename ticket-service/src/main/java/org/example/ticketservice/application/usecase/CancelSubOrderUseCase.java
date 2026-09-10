package org.example.ticketservice.application.usecase;

import java.util.UUID;

public interface CancelSubOrderUseCase {

    void cancel(UUID subOrderId, String reason);
}
