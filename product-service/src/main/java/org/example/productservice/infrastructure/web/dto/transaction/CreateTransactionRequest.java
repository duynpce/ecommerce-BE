package org.example.productservice.infrastructure.web.dto.transaction;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CreateTransactionRequest(
        @NotEmpty List<@Valid CreateTransactionItemRequest> items,
        @Size(max = 3) List<@NotBlank @Size(max = 50) String> voucherCodes,
        @NotBlank
        @Size(max = 25)
        @Pattern(regexp = "^[0-9+()\\-\\s]{7,25}$", message = "must be a valid phone number")
        String phoneNumber,
        @NotBlank @Size(max = 500) String address
) {
    public List<CreateTransactionItemRequest> getItemList() {
        if (items != null && !items.isEmpty()) {
            return items;
        }

        return new ArrayList<>();
    }
}
