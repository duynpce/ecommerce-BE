package org.example.productservice.infrastructure.web.dto.suborder;

import jakarta.validation.constraints.NotNull;
import org.example.productservice.domain.constant.SubOrderStatus;

public record UpdateSubOrderStatusRequest(
        @NotNull(message = "Status cannot be null")
        SubOrderStatus status
) {}
