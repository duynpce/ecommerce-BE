package org.example.ticketservice.application.usecase;

import org.example.ticketservice.application.command.CreateProductReviewCommand;

import java.util.UUID;

public interface CreateProductReviewUseCase {

    void create(UUID subOrderId, CreateProductReviewCommand command);
}
