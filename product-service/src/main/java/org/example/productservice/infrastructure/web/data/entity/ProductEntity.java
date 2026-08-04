package org.example.productservice.infrastructure.web.data.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.productservice.domain.constant.ProductCategory;
import org.example.productservice.domain.constant.ProductStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
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
@SuperBuilder
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

    private ProductStatus status = ProductStatus.PENDING;

    private Map<String, String> attributes;

    /** Cumulative average star rating (0.0 – 5.0). Defaults to 0. */
    @Builder.Default
    private Double rating = 0.0;

    /** Total number of units sold across all orders. Defaults to 0. */
    @Builder.Default
    private Integer soldQuantity = 0;

    /** Number of 1-star reviews. */
    @Builder.Default
    private Integer oneStarRatingCount = 0;

    /** Number of 2-star reviews. */
    @Builder.Default
    private Integer twoStarRatingCount = 0;

    /** Number of 3-star reviews. */
    @Builder.Default
    private Integer threeStarRatingCount = 0;

    /** Number of 4-star reviews. */
    @Builder.Default
    private Integer fourStarRatingCount = 0;

    /** Number of 5-star reviews. */
    @Builder.Default
    private Integer fiveStarRatingCount = 0;

    @Transient
    private ShopEntity shop;
}