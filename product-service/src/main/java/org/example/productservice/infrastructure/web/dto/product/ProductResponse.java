package org.example.productservice.infrastructure.web.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.productservice.domain.constant.ProductCategory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private UUID id;
    private UUID contributorId;
    private UUID shopId;
    private List<String> imgUrls;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private ProductCategory category;
    private Map<String, String> attributes;
}
