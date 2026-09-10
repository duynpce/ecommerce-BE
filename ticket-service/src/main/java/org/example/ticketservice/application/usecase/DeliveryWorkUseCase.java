package org.example.ticketservice.application.usecase;

import org.example.ticketservice.domain.model.DeliveryWorkItem;

import java.util.List;
import java.util.UUID;

public interface DeliveryWorkUseCase {
    List<DeliveryWorkItem> getWork(UUID shipperId);
    void acceptPickup(String taskId, UUID shipperId);
    void completeDelivery(String taskId, UUID shipperId, String outcome);
}
