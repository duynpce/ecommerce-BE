package org.example.ticketservice.infrastructure.auth.httpclient;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface AuthHttpClient {

    @PostExchange("api/v1/auth/local/promote/{userId}")
    void promoteAccountToContributor(@PathVariable("userId") String userId);

    @PostExchange("api/v1/auth/local/promote/shipper/{userId}")
    void promoteAccountToShipper(@PathVariable("userId") String userId);
}
