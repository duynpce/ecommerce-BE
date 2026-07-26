package org.example.productservice.application.command;

import org.example.productservice.domain.constant.ShopStatus;
import org.example.productservice.domain.valueobject.Address;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record UpdateShopCommand(
        UUID id,
        UUID senderId,
        MultipartFile logo,
        String name,
        String description,
        Address pickUpAddress,
        ShopStatus status
) {}
