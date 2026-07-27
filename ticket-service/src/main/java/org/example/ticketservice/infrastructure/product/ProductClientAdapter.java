package org.example.ticketservice.infrastructure.product;

import lombok.RequiredArgsConstructor;
import org.example.ticketservice.application.client.ProductClient;
import org.example.ticketservice.domain.constant.TransactionStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter that bridges the application-layer {@link ProductClient} port
 * to the Spring HTTP Interface {@link ProductHttpClient}.
 */
@Component
@RequiredArgsConstructor
public class ProductClientAdapter implements ProductClient {

    private final ProductHttpClient productHttpClient;

    @Override
    public void updateTransactionStatus(UUID transactionId, TransactionStatus status) {
        productHttpClient.updateTransactionStatus(transactionId, new UpdateTransactionStatusRequest(status));
    }

    @Override
    public void returnTransaction(UUID transactionId) {
        productHttpClient.returnTransaction(transactionId);
    }
}

