package org.example.ticketservice.infrastructure.product;

import lombok.RequiredArgsConstructor;
import org.example.ticketservice.application.client.ProductClient;
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
    public void approve(UUID transactionId) {
        productHttpClient.approve(transactionId);
    }

    @Override
    public void reject(UUID transactionId) {
        productHttpClient.reject(transactionId);
    }

    @Override
    public void deliver(UUID transactionId) {
        productHttpClient.deliver(transactionId);
    }

    @Override
    public void complete(UUID transactionId) {
        productHttpClient.complete(transactionId);
    }

    @Override
    public void returnTransaction(UUID transactionId) {
        productHttpClient.returnTransaction(transactionId);
    }
}
