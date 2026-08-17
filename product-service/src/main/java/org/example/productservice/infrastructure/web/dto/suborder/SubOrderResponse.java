package org.example.productservice.infrastructure.web.dto.suborder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.productservice.domain.constant.SubOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubOrderResponse {
    private UUID id;
    private UUID transactionId;
    private UUID shopId;
    private UUID customerId;
    private UUID contributorId;
    private List<ProductSnapshotResponse> items;
    private BigDecimal subTotalAmount;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String note;
    private SubOrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
