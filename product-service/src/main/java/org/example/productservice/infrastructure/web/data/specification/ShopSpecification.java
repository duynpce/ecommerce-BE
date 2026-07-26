package org.example.productservice.infrastructure.web.data.specification;

import org.example.productservice.application.criteria.ShopSearchCriteria;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

public class ShopSpecification {

    private ShopSpecification() {}

    /**
     * Builds a {@link Query} from the given criteria.
     * Only non-null fields are added — all conditions are AND-ed together.
     */
    public static Query fromCriteria(ShopSearchCriteria criteria) {
        List<Criteria> conditions = new ArrayList<>();

        if (criteria.name() != null && !criteria.name().isBlank()) {
            // Case-insensitive partial match on name
            conditions.add(Criteria.where("name").regex(criteria.name(), "i"));
        }

        if (criteria.contributorId() != null) {
            conditions.add(Criteria.where("contributorId").is(criteria.contributorId()));
        }

        if (criteria.status() != null) {
            conditions.add(Criteria.where("status").is(criteria.status()));
        }

        Query query = new Query();
        if (!conditions.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(conditions.toArray(new Criteria[0])));
        }

        return query;
    }
}
