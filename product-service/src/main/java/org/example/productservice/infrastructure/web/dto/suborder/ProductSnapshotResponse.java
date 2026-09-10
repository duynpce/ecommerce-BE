package org.example.productservice.infrastructure.web.dto.suborder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.productservice.domain.constant.ProductSnapshotStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSnapshotResponse {
    private UUID id;
    private UUID productId;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String imageUrl;
    private ProductSnapshotStatus status;
    private Boolean isReviewed;
    private Instant deliveredAt;
    private BigDecimal subtotal;
}
