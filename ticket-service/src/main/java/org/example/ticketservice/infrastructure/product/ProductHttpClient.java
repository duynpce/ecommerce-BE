package org.example.ticketservice.infrastructure.product;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;

import org.springframework.web.service.annotation.PostExchange;

import java.util.UUID;

/**
 * Spring HTTP Interface client for product-service transaction endpoints.
 * Used by Camunda delegates to update transaction status during buying-items-procedure.
 */
@HttpExchange
public interface ProductHttpClient {

    @PatchExchange("/api/v1/transactions/{id}")
    void updateTransactionStatus(@PathVariable UUID id,
                                 @RequestBody UpdateTransactionStatusRequest request);

    @PostExchange("/api/v1/transactions/{id}/return")
    void returnTransaction(@PathVariable UUID id);
}

