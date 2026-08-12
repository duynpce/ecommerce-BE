package org.example.productservice.infrastructure.web.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.productservice.domain.constant.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private UUID id;
    private UUID customerId;
    private List<UUID> subOrderIds;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private UUID voucherId;
    private String voucherCode;
    private String description;
    private TransactionStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
