package org.example.ticketservice.infrastructure.product.dto;

import org.example.ticketservice.domain.constant.SubOrderStatus;

public record UpdateSubOrderStatusRequest(SubOrderStatus status) {
}
