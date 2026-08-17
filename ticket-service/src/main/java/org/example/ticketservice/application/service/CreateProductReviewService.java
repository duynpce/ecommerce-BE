package org.example.ticketservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.example.ticketservice.application.client.ProductClient;
import org.example.ticketservice.application.command.CreateProductReviewCommand;
import org.example.ticketservice.application.usecase.CreateProductReviewUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateProductReviewService implements CreateProductReviewUseCase {

    private static final String REVIEW_TASK_KEY = "review-product";

    private final TaskService taskService;
    private final ProductClient productClient;

    @Override
    public void create(UUID subOrderId, CreateProductReviewCommand command) {
        String subOrderIdString = subOrderId.toString();

        List<Task> matchingTasks = taskService.createTaskQuery()
                .taskDefinitionKey(REVIEW_TASK_KEY)
                .list()
                .stream()
                .filter(candidate -> subOrderIdString.equals(
                        taskService.getVariable(candidate.getId(), "subOrderId")))
                .filter(candidate -> command.snapshotId().toString().equals(
                        taskService.getVariable(candidate.getId(), "snapshotId")))
                .toList();

        if (matchingTasks.isEmpty()) {
            throw new IllegalStateException(
                    "No pending product review task found for subOrderId=" + subOrderId
                            + ", snapshotId=" + command.snapshotId());
        }
        if (matchingTasks.size() > 1) {
            throw new IllegalStateException(
                    "Multiple pending product review tasks found for subOrderId=" + subOrderId
                            + ", snapshotId=" + command.snapshotId());
        }

        Task reviewTask = matchingTasks.getFirst();

        validateReviewContext(reviewTask, subOrderId, command);

        // Product-service validates the transaction/snapshot relationship, persists
        // the review, and updates product/shop rating aggregates.
        productClient.createProductReview(command);

        String snapshotId = command.snapshotId().toString();
        taskService.complete(reviewTask.getId(), Map.of("currentSnapshotId", snapshotId));

        log.info("[buying-items] Product review created: subOrderId={}, snapshotId={}, productId={}",
                subOrderId, command.snapshotId(), command.productId());
    }

    private void validateReviewContext(
            Task reviewTask, UUID subOrderId, CreateProductReviewCommand command) {
        Object transactionId = taskService.getVariable(reviewTask.getId(), "transactionId");
        if (!command.transactionId().toString().equals(transactionId)) {
            throw new IllegalArgumentException(
                    "Review transaction does not match the pending task for subOrderId: " + subOrderId);
        }

        Object snapshotsValue = taskService.getVariable(
                reviewTask.getId(), "snapshots_" + subOrderId);
        if (!(snapshotsValue instanceof List<?> snapshots)) {
            throw new IllegalStateException(
                    "Snapshot data is missing for subOrderId: " + subOrderId);
        }

        boolean matchingSnapshot = snapshots.stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .anyMatch(snapshot -> command.snapshotId().toString().equals(snapshot.get("snapshotId"))
                        && command.productId().toString().equals(snapshot.get("productId")));

        if (!matchingSnapshot) {
            throw new IllegalArgumentException(
                    "Product and snapshot do not belong to subOrderId: " + subOrderId);
        }
    }
}
