package org.example.productservice.infrastructure.web.data.springdata;

import org.example.productservice.domain.constant.ProductCategory;
import org.example.productservice.infrastructure.web.data.entity.ProductEntity;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// JpaSpecificationExecutor removed — complex queries are handled via MongoTemplate in the adapter
public interface SpringDataProductRepository extends MongoRepository<ProductEntity, UUID> {
    List<ProductEntity> findByContributorId(UUID contributorId);
    List<ProductEntity> findByCategory(ProductCategory category);

    @Aggregation(pipeline = {
            "{ '$match': { '_id': ?0 } }",
            "{ '$lookup': { 'from': 'shops', 'localField': 'shopId', 'foreignField': '_id', 'as': 'shop' } }",
            "{ '$unwind': { 'path': '$shop', 'preserveNullAndEmptyArrays': true } }"
    })
    Optional<ProductEntity> findByIdWithShop(UUID id);
}
