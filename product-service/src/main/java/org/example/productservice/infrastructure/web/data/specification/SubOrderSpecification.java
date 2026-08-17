package org.example.productservice.infrastructure.web.data.specification;

import org.example.productservice.application.criteria.SubOrderSearchCriteria;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

public final class SubOrderSpecification {

    private SubOrderSpecification() {}

    public static Query fromCriteria(SubOrderSearchCriteria criteria) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (criteria.customerId() != null) {
            criteriaList.add(Criteria.where("customerId").is(criteria.customerId()));
        }

        if (criteria.shopId() != null) {
            criteriaList.add(Criteria.where("shopId").is(criteria.shopId()));
        }

        if (criteria.contributorId() != null) {
            criteriaList.add(Criteria.where("contributorId").is(criteria.contributorId()));
        }

        if (criteria.transactionId() != null) {
            criteriaList.add(Criteria.where("transactionId").is(criteria.transactionId()));
        }

        if (criteria.status() != null) {
            criteriaList.add(Criteria.where("status").is(criteria.status()));
        }

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
