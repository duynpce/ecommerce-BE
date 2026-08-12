package org.example.productservice.infrastructure.web.data.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.productservice.domain.constant.ProductSnapshotStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * MongoDB-embedded document that represents a product snapshot stored inside
 * a {@link SubOrderEntity} or {@link CartEntity}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductSnapshotEntity extends BaseEntity {

    @Builder.Default
    private UUID id = UUID.randomUUID();

    /** Reference to the source product (for review / reorder links). */
    private UUID productId;

    /** Product name at the time of the order. */
    private String name;

    /** Unit price at the time of the order. */
    private BigDecimal price;

    /** Number of units in this line item. */
    private Integer quantity;

    /** Thumbnail URL captured at order time. */
    private String imageUrl;

    @Builder.Default
    private ProductSnapshotStatus status = ProductSnapshotStatus.PENDING;

    @Builder.Default
    private Boolean isReviewed = false;

    private Instant deliveredAt;
}
