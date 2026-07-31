package org.example.productservice.infrastructure.web.data.specification;

import org.example.productservice.application.criteria.ProductSearchCriteria;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

public final class ProductSpecification {

    private ProductSpecification() {}

    public static Query fromCriteria(ProductSearchCriteria criteria) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (criteria.name() != null && !criteria.name().isBlank()) {
            // Case-insensitive partial match — equivalent to JPA cb.like(cb.lower(...))
            criteriaList.add(Criteria.where("name").regex(criteria.name(), "i"));
        }

        if (criteria.category() != null) {
            criteriaList.add(Criteria.where("category").is(criteria.category()));
        }

        if (criteria.contributorId() != null) {
            criteriaList.add(Criteria.where("contributorId").is(criteria.contributorId()));
        }

        if(criteria.shopId() != null) {
            criteriaList.add(Criteria.where("shopId").is(criteria.shopId()));
        }

        // Merge min/max price into a single Criteria to avoid duplicate-key error
        if (criteria.minPrice() != null && criteria.maxPrice() != null) {
            criteriaList.add(Criteria.where("price")
                    .gte(criteria.minPrice())
                    .lte(criteria.maxPrice()));
        } else if (criteria.minPrice() != null) {
            criteriaList.add(Criteria.where("price").gte(criteria.minPrice()));
        } else if (criteria.maxPrice() != null) {
            criteriaList.add(Criteria.where("price").lte(criteria.maxPrice()));
        }

        // Merge createdFrom/createdTo into a single Criteria for the same reason
        if (criteria.createdFrom() != null && criteria.createdTo() != null) {
            criteriaList.add(Criteria.where("createdAt")
                    .gte(criteria.createdFrom())
                    .lte(criteria.createdTo()));
        } else if (criteria.createdFrom() != null) {
            criteriaList.add(Criteria.where("createdAt").gte(criteria.createdFrom()));
        } else if (criteria.createdTo() != null) {
            criteriaList.add(Criteria.where("createdAt").lte(criteria.createdTo()));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList));
        }
        return query;
    }
}
