package org.example.authservice.infrastructure.user.httpclient;

import jakarta.validation.Valid;
import org.example.authservice.infrastructure.web.dto.CreateAccountRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface UserHttpClient {
    @PostExchange("/api/v1/users/account-profiles")
    public void createAccount(@RequestBody @Valid CreateAccountRequest request);
}
