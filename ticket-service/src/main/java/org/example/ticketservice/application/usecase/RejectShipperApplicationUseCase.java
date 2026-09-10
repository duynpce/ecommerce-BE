package org.example.ticketservice.application.usecase;

import org.example.ticketservice.application.command.RejectShipperApplicationCommand;

public interface RejectShipperApplicationUseCase {
    void reject(RejectShipperApplicationCommand command);
}
