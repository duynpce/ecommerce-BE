package org.example.reportservice.infrastructure.product.httpclient;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.example.reportservice.domain.constant.TransactionStatus;
import org.example.reportservice.infrastructure.product.dto.TransactionReportResponse;
import org.example.reportservice.infrastructure.web.dto.ResponseDto;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface ProductHttpClient {

    /**
     * Calls product-service GET /api/v1/products/transactions/search.
     * Flat @RequestParam bindings — Spring's declarative HTTP client resolves
     * each param individually, matching the TransactionFilter fields one-to-one.
     */
    @GetExchange("/api/v1/products/transactions/contributor/search")
    ResponseDto<List<TransactionReportResponse>> searchTransactions(
            @RequestParam Integer page,
            @RequestParam Integer limit,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) LocalDate createdFrom,
            @RequestParam(required = false) LocalDate createdTo);
}
