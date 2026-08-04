package org.example.reportservice.application.client;

import java.util.List;
import org.example.reportservice.infrastructure.product.dto.TransactionReportFilter;
import org.example.reportservice.infrastructure.product.dto.TransactionReportResponse;
import org.example.reportservice.infrastructure.web.dto.ResponseDto;

public interface ProductClient {

    ResponseDto<List<TransactionReportResponse>> searchTransactions(TransactionReportFilter filter);
}
