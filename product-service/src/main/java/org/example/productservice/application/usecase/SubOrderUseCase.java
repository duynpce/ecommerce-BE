package org.example.productservice.application.usecase;

import org.example.productservice.application.command.CreateSubOrderCommand;
import org.example.productservice.application.command.PageCommand;
import org.example.productservice.application.command.UpdateSubOrderCommand;
import org.example.productservice.application.criteria.SubOrderSearchCriteria;
import org.example.productservice.domain.constant.ProductSnapshotStatus;
import org.example.productservice.domain.constant.SubOrderStatus;
import org.example.productservice.domain.model.SubOrder;

import java.util.List;
import java.util.UUID;

public interface SubOrderUseCase {
    SubOrder create(CreateSubOrderCommand command);
    SubOrder findById(UUID id);
    List<SubOrder> findByTransactionId(UUID transactionId);
    List<SubOrder> findByShopId(UUID shopId);
    List<SubOrder> findByCustomerId(UUID customerId);
    SubOrder update(UpdateSubOrderCommand command);
    SubOrder updateStatus(UUID id, SubOrderStatus status);
    SubOrder updateSnapshotStatus(UUID subOrderId, UUID snapshotId, ProductSnapshotStatus status);
    void delete(UUID id);
    PageCommand<SubOrder> search(SubOrderSearchCriteria criteria);
}
