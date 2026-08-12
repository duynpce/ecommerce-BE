package org.example.productservice.infrastructure.web.data.springdata;

import org.example.productservice.infrastructure.web.data.entity.SubOrderEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataSubOrderRepository extends MongoRepository<SubOrderEntity, UUID> {
    List<SubOrderEntity> findByTransactionId(UUID transactionId);
    List<SubOrderEntity> findByShopId(UUID shopId);
    List<SubOrderEntity> findByCustomerId(UUID customerId);
}
