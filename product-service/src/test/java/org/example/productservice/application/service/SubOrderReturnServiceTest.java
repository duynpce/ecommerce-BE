package org.example.productservice.application.service;

import org.example.productservice.application.repository.ProductRepository;
import org.example.productservice.application.repository.ShopRepository;
import org.example.productservice.application.repository.SubOrderRepository;
import org.example.productservice.application.repository.TransactionRepository;
import org.example.productservice.domain.constant.ProductSnapshotStatus;
import org.example.productservice.domain.constant.SubOrderStatus;
import org.example.productservice.domain.model.Product;
import org.example.productservice.domain.model.ProductSnapshot;
import org.example.productservice.domain.model.SubOrder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SubOrderReturnServiceTest {

    @Test
    void returningSnapshotRestoresStockOnceAndRecalculatesSubOrder() {
        UUID subOrderId = UUID.randomUUID();
        UUID snapshotId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ProductSnapshot returnedSnapshot = new ProductSnapshot(
                snapshotId, productId, "Returned item", BigDecimal.TEN, 2, null);
        returnedSnapshot.setStatus(ProductSnapshotStatus.RECEIVED);
        ProductSnapshot completedSnapshot = new ProductSnapshot(
                UUID.randomUUID(), UUID.randomUUID(), "Kept item", BigDecimal.ONE, 1, null);
        completedSnapshot.setStatus(ProductSnapshotStatus.COMPLETED);

        SubOrder subOrder = new SubOrder();
        subOrder.setId(subOrderId);
        subOrder.setItems(List.of(returnedSnapshot, completedSnapshot));
        SubOrderRepository subOrderRepository = mock(SubOrderRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        Product product = mock(Product.class);
        when(subOrderRepository.findById(subOrderId)).thenReturn(Optional.of(subOrder));
        when(subOrderRepository.save(subOrder)).thenReturn(subOrder);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(product.getQuantity()).thenReturn(5);

        SubOrderService service = new SubOrderService(
                subOrderRepository,
                productRepository,
                mock(ShopRepository.class),
                mock(TransactionRepository.class));

        service.returnSnapshot(subOrderId, snapshotId);
        service.returnSnapshot(subOrderId, snapshotId);

        assertEquals(ProductSnapshotStatus.RETURNED, returnedSnapshot.getStatus());
        assertEquals(SubOrderStatus.PARTIALLY_RETURNED, subOrder.getStatus());
        verify(product).setQuantity(7);
        verify(productRepository).save(product);
        verify(subOrderRepository).save(subOrder);
    }
}
