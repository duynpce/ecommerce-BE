package org.example.productservice.infrastructure.web.data.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.productservice.domain.constant.ShopStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "shops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ShopEntity extends BaseEntity {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Indexed
    private UUID contributorId;

    private String name;

    private String description;

    private String logoUrl;

    private String pickUpAddress;

    @Builder.Default
    private ShopStatus status = ShopStatus.ACTIVE;

    /** Cumulative average star rating across all shop products (0.0 – 5.0). */
    @Builder.Default
    private Double rating = 0.0;

    /** Total number of units sold across all products in this shop. */
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
}