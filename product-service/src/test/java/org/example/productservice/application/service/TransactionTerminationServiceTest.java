package org.example.productservice.application.service;

import org.example.productservice.application.client.TicketClient;
import org.example.productservice.application.mapper.SubOrderMapper;
import org.example.productservice.application.mapper.TransactionMapper;
import org.example.productservice.application.repository.ProductRepository;
import org.example.productservice.application.repository.ShopRepository;
import org.example.productservice.application.repository.SubOrderRepository;
import org.example.productservice.application.repository.TransactionRepository;
import org.example.productservice.application.usecase.SubOrderUseCase;
import org.example.productservice.application.usecase.VoucherUseCase;
import org.example.productservice.domain.constant.ProductSnapshotStatus;
import org.example.productservice.domain.constant.SubOrderStatus;
import org.example.productservice.domain.constant.TransactionStatus;
import org.example.productservice.domain.model.ProductSnapshot;
import org.example.productservice.domain.model.Shop;
import org.example.productservice.domain.model.SubOrder;
import org.example.productservice.domain.model.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TransactionTerminationServiceTest {

    @Test
    void rejectedTransactionCancelsEverySubOrderAndRecordsTrigger() {
        UUID transactionId = UUID.randomUUID();
        UUID rejectedSubOrderId = UUID.randomUUID();
        UUID siblingSubOrderId = UUID.randomUUID();
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setSubOrderIds(List.of(rejectedSubOrderId, siblingSubOrderId));

        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        SubOrderUseCase subOrderUseCase = mock(SubOrderUseCase.class);
        ShopRepository shopRepository = mock(ShopRepository.class);
        UUID shopId = UUID.randomUUID();
        ProductSnapshot snapshot = new ProductSnapshot(
                UUID.randomUUID(), UUID.randomUUID(), "Coffee Beans", BigDecimal.TEN, 1, null);
        SubOrder rejectedSubOrder = new SubOrder();
        rejectedSubOrder.setId(rejectedSubOrderId);
        rejectedSubOrder.setShopId(shopId);
        rejectedSubOrder.setItems(List.of(snapshot));
        Shop shop = new Shop();
        shop.setName("Green Market");
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(subOrderUseCase.findById(rejectedSubOrderId)).thenReturn(rejectedSubOrder);
        when(shopRepository.findById(shopId)).thenReturn(Optional.of(shop));

        TransactionService service = new TransactionService(
                transactionRepository,
                mock(ProductRepository.class),
                shopRepository,
                subOrderUseCase,
                mock(TransactionMapper.class),
                mock(SubOrderMapper.class),
                mock(TicketClient.class),
                mock(VoucherUseCase.class));

        service.complete(
                transactionId,
                TransactionStatus.REJECTED,
                "Contributor cannot fulfil the order.",
                rejectedSubOrderId);

        assertEquals(TransactionStatus.REJECTED, transaction.getStatus());
        assertEquals(
                "Transaction rejected because shop \"Green Market\" for Coffee Beans was rejected. "
                        + "Contributor cannot fulfil the order.",
                transaction.getStatusReason());
        assertEquals(rejectedSubOrderId, transaction.getTriggerSubOrderId());
        verify(subOrderUseCase).cancelForTransactionTermination(
                rejectedSubOrderId,
                "This shop order caused the transaction rejection. "
                        + "Contributor cannot fulfil the order.");
        verify(subOrderUseCase).cancelForTransactionTermination(
                siblingSubOrderId,
                "Cancelled because shop \"Green Market\" for Coffee Beans was rejected. "
                        + "Contributor cannot fulfil the order.");
    }

    @Test
    void cascadeConvertsRejectedSubOrderToCancelledWithoutRestoringStockTwice() {
        UUID subOrderId = UUID.randomUUID();
        ProductSnapshot snapshot = new ProductSnapshot(
                UUID.randomUUID(), UUID.randomUUID(), "Item", BigDecimal.ONE, 1, null);
        snapshot.setStatus(ProductSnapshotStatus.REJECTED);
        SubOrder subOrder = new SubOrder();
        subOrder.setId(subOrderId);
        subOrder.setItems(List.of(snapshot));
        subOrder.setStatus(SubOrderStatus.REJECTED);

        SubOrderRepository subOrderRepository = mock(SubOrderRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        when(subOrderRepository.findById(subOrderId)).thenReturn(Optional.of(subOrder));
        when(subOrderRepository.save(subOrder)).thenReturn(subOrder);

        SubOrderService service = new SubOrderService(
                subOrderRepository,
                productRepository,
                mock(ShopRepository.class),
                mock(TransactionRepository.class));

        service.cancelForTransactionTermination(
                subOrderId, "Parent transaction was rejected.");

        assertEquals(SubOrderStatus.CANCELLED, subOrder.getStatus());
        assertEquals(ProductSnapshotStatus.CANCELLED, snapshot.getStatus());
        assertEquals("Parent transaction was rejected.", subOrder.getStatusReason());
        verifyNoInteractions(productRepository);
        verify(subOrderRepository).save(subOrder);
    }
}
