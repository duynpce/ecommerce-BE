package org.example.productservice.infrastructure.web.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.productservice.domain.constant.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private UUID id;
    private UUID productId;
    private UUID contributorId;
    private UUID customerId;
    private Integer quantity;
    private BigDecimal totalAmount;
    private TransactionStatus status;
    private Instant createdAt;
}
