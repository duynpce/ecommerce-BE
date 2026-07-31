package org.example.productservice.infrastructure.web.data.entity;

import lombok.*;
import org.example.productservice.domain.constant.ProductCategory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Document(collection = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity extends BaseEntity {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Indexed
    private UUID shopId;

    @Indexed
    private UUID contributorId;

    private List<String> imgUrls;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer quantity;

    private ProductCategory category;

    // MongoDB natively stores nested objects/maps — no special type hint needed
    private Map<String, String> attributes;
}
