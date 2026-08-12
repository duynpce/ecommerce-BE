package org.example.productservice.infrastructure.web.dto.suborder;

import org.example.productservice.domain.constant.SubOrderStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

public record SubOrderFilter(
        UUID shopId,
        UUID transactionId,
        SubOrderStatus status,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate createdFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate createdTo,

        int page,
        int limit
) {
    public SubOrderFilter {
        if (page < 0) page = 0;
        if (limit <= 0) limit = 20;
    }
}
