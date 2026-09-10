package org.example.reportservice.infrastructure.product;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.reportservice.application.client.ProductClient;
import org.example.reportservice.infrastructure.product.dto.TransactionReportFilter;
import org.example.reportservice.infrastructure.product.dto.TransactionReportResponse;
import org.example.reportservice.infrastructure.product.httpclient.ProductHttpClient;
import org.example.reportservice.infrastructure.web.dto.ResponseDto;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductAdapter implements ProductClient {

    private final ProductHttpClient productHttpClient;

    @Override
    public ResponseDto<List<TransactionReportResponse>> searchTransactions(TransactionReportFilter filter) {
        ResponseDto<List<TransactionReportResponse>> response = productHttpClient.searchTransactions(
                filter.page(),
                filter.limit(),
                filter.productId(),
                filter.status(),
                filter.createdFrom(),
                filter.createdTo());

        if (response == null || response.getData() == null) {
            return ResponseDto.<List<TransactionReportResponse>>builder().build();
        }
        return response;
    }
}
