package org.example.productservice.application.service;

import org.example.productservice.application.command.CreateProductReviewCommand;
import org.example.productservice.application.mapper.ProductReviewMapper;
import org.example.productservice.application.repository.ProductRepository;
import org.example.productservice.application.repository.ProductReviewRepository;
import org.example.productservice.application.repository.ShopRepository;
import org.example.productservice.application.repository.SubOrderRepository;
import org.example.productservice.domain.model.ProductReview;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductReviewServiceTest {

    @Mock
    private ProductReviewRepository productReviewRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductReviewMapper productReviewMapper;
    @Mock
    private ShopRepository shopRepository;
    @Mock
    private SubOrderRepository subOrderRepository;

    @Test
    void identicalRetryReturnsExistingReviewWithoutApplyingAggregatesAgain() {
        UUID productId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UUID snapshotId = UUID.randomUUID();
        CreateProductReviewCommand command = new CreateProductReviewCommand(
                productId, userId, transactionId, snapshotId, 5, "Excellent");
        ProductReview existing = new ProductReview(
                UUID.randomUUID(), productId, userId, transactionId, snapshotId, 5, "Excellent");

        when(productReviewRepository.findByUserIdAndTransactionIdAndSnapshotId(
                userId, transactionId, snapshotId)).thenReturn(Optional.of(existing));

        ProductReviewService service = new ProductReviewService(
                productReviewRepository,
                productRepository,
                productReviewMapper,
                shopRepository,
                subOrderRepository);

        ProductReview result = service.create(command);

        assertSame(existing, result);
        verifyNoInteractions(productRepository, productReviewMapper, shopRepository, subOrderRepository);
    }
}
