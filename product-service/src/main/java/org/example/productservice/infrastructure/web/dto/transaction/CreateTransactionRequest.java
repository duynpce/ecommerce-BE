package org.example.productservice.infrastructure.web.dto.transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CreateTransactionRequest(
        List<CreateTransactionItemRequest> items
) {
    public List<CreateTransactionItemRequest> getItemList() {
        if (items != null && !items.isEmpty()) {
            return items;
        }

        return new ArrayList<>();
    }
}
