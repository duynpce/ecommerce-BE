package org.example.productservice.infrastructure.web.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.productservice.domain.constant.TransactionStatus;
import org.example.productservice.infrastructure.web.dto.voucher.VoucherSnapshotResponse;

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
    private BigDecimal subtotalAmount;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private List<VoucherSnapshotResponse> vouchers;
    private String phoneNumber;
    private String address;
    private String description;
    private TransactionStatus status;
    private String statusReason;
    private UUID triggerSubOrderId;
    private Instant createdAt;
    private Instant updatedAt;
}
