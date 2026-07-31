package org.example.ticketservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body for POST /transaction-tickets/{transactionId}/delivery
 * Buyer confirms the delivery outcome.
 * status must be one of: RECEIVED, NOT_RECEIVED, RETURNED
 */
public record ConfirmDeliveryRequest(

        @NotBlank(message = "status cannot be blank")
        @Pattern(
                regexp = "RECEIVED|NOT_RECEIVED|RETURNED",
                message = "status must be one of: RECEIVED, NOT_RECEIVED, RETURNED"
        )
        String status
) {}
