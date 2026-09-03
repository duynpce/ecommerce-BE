package org.example.ticketservice.application.usecase;

import org.example.ticketservice.domain.model.ReturnWorkItem;

import java.util.List;
import java.util.UUID;

public interface ReturnWorkUseCase {
    List<ReturnWorkItem> getPendingReturns(UUID contributorId);
    void confirmReturn(String taskId, UUID contributorId, boolean received);
}
