package org.example.productservice.application.repository;

import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.criteria.SubOrderSearchCriteria;
import org.example.productservice.domain.model.SubOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubOrderRepository {
    SubOrder save(SubOrder subOrder);
    Optional<SubOrder> findById(UUID id);
    List<SubOrder> findByTransactionId(UUID transactionId);
    List<SubOrder> findByShopId(UUID shopId);
    List<SubOrder> findByCustomerId(UUID customerId);
    boolean existsById(UUID id);
    void deleteById(UUID id);
    PageCommand<SubOrder> search(SubOrderSearchCriteria criteria);
}
