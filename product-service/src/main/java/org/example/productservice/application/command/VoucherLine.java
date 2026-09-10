package org.example.productservice.application.command;

import org.example.productservice.domain.model.Product;

public record VoucherLine(Product product, int quantity) {}
