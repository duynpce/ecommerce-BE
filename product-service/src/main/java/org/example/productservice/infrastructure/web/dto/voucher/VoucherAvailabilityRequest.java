package org.example.productservice.infrastructure.web.dto.voucher;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.example.productservice.infrastructure.web.dto.transaction.CreateTransactionItemRequest;

import java.util.List;

public record VoucherAvailabilityRequest(
        @NotEmpty List<@Valid CreateTransactionItemRequest> items
) {}
