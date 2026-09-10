package org.example.productservice.infrastructure.web.dto.voucher;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.example.productservice.infrastructure.web.dto.transaction.CreateTransactionItemRequest;

import java.util.List;

public record VoucherPreviewRequest(
        @NotBlank String code,
        @NotEmpty List<@Valid CreateTransactionItemRequest> items
) {}
