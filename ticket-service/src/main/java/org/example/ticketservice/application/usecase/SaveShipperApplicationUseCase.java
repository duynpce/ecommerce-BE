package org.example.ticketservice.application.usecase;

import org.example.ticketservice.application.command.SaveShipperApplicationCommand;

public interface SaveShipperApplicationUseCase {
    void execute(SaveShipperApplicationCommand command);
}
