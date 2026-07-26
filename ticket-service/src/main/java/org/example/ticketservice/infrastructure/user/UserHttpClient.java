package org.example.ticketservice.infrastructure.user;

import jakarta.validation.Valid;
import org.example.ticketservice.infrastructure.web.dto.CreateContributorProfileRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface UserHttpClient {

    @PostExchange("api/v1/users/contributor-profiles")
    void createContributorProfile(@RequestBody @Valid CreateContributorProfileRequest request);
}
