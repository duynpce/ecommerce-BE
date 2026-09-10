package org.example.ticketservice.application.usecase;

import org.example.ticketservice.application.command.ApproveShipperApplicationCommand;

public interface ApproveShipperApplicationUseCase {
    void approve(ApproveShipperApplicationCommand command);
}
