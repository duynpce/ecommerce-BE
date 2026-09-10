package org.example.productservice.infrastructure.web.data.specification;

import org.example.productservice.application.criteria.TransactionSearchCriteria;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

public final class TransactionSpecification {

    private TransactionSpecification() {}

    public static Query fromCriteria(TransactionSearchCriteria criteria) {
        List<Criteria> criteriaList = new ArrayList<>();

        // READ_SELF: restrict to transactions where the user is either seller or buyer
        if (criteria.userId() != null) {
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("customerId").is(criteria.userId())
            ));
        }

        if (criteria.contributorId() != null) {
            criteriaList.add(Criteria.where("contributorId").is(criteria.contributorId()));
        }

        if (criteria.productId() != null) {
            criteriaList.add(Criteria.where("items.productId").is(criteria.productId()));
        }

        if (criteria.status() != null) {
            criteriaList.add(Criteria.where("status").is(criteria.status()));
        }

        // Merge createdFrom/createdTo into a single Criteria to avoid duplicate-key error
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
