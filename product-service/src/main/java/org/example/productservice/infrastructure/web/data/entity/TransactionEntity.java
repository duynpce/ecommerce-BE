package org.example.productservice.infrastructure.web.data.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.productservice.domain.constant.TransactionStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;

@Document(collection = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TransactionEntity extends BaseEntity {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Indexed
    private UUID productId;

    @Indexed
    private UUID contributorId;

    // customerId was referenced in TransactionSpecification but missing from the
    // original entity — added here so the userId seller/buyer filter works correctly
    @Indexed
    private UUID customerId;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal totalAmount;

    private String description;

    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

}
