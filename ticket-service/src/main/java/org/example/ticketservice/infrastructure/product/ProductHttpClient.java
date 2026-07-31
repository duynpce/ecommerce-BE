package org.example.ticketservice.infrastructure.product;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.UUID;

/**
 * Spring HTTP Interface client for product-service transaction state-transition endpoints.
 * No request body needed — the endpoint URL itself encodes the intended transition.
 */
@HttpExchange
public interface ProductHttpClient {

    @PatchExchange("/api/v1/products/transactions/{id}/approve")
    void approve(@PathVariable UUID id);

    @PatchExchange("/api/v1/products/transactions/{id}/reject")
    void reject(@PathVariable UUID id);

    @PatchExchange("/api/v1/products/transactions/{id}/deliver")
    void deliver(@PathVariable UUID id);

    @PatchExchange("/api/v1/products/transactions/{id}/complete")
    void complete(@PathVariable UUID id);

    @PostExchange("/api/v1/products/transactions/{id}/return")
    void returnTransaction(@PathVariable UUID id);
}
