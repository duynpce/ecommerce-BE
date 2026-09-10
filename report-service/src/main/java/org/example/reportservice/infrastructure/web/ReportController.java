package org.example.reportservice.infrastructure.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.reportservice.application.usecase.JasperReportUseCase;
import org.example.reportservice.infrastructure.product.dto.TransactionReportFilter;
import org.example.reportservice.infrastructure.user.dto.AccountReportFilter;
import org.example.reportservice.infrastructure.web.dto.ReportFilePropRes;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

/**
 * Final path (server.servlet.context-path=/api/v1/reports):
 *   GET /api/v1/reports/accounts/export?format=pdf|xlsx&page=0&limit=1000&...
 * <p>
 * Pulls account data through the UserClient port (backed by UserHttpClient ->
 * user-service's GET /accounts/report) and renders it via the account_report
 * Jasper template.
 */
@RequiredArgsConstructor
@RestController
public class ReportController {


    private final JasperReportUseCase  jasperReportUseCase;


    @GetMapping("/accounts/export")
    @PreAuthorize("hasAuthority('EXPORT:DOWNLOAD_ALL')")
    public ResponseEntity<byte[]> exportAccountReport(
            @Valid @ModelAttribute AccountReportFilter filter) throws Exception {


        ReportFilePropRes reportFilePropRes = jasperReportUseCase.generateAccountReport(filter);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(reportFilePropRes.fileName()).build());

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(reportFilePropRes.mediaType())
                .body((reportFilePropRes.fileBytes()));
    }

    @GetMapping("/transactions/export")
    @PreAuthorize("hasAuthority('EXPORT:DOWNLOAD_SELF')")
    public ResponseEntity<byte[]> exportTransactionReport(
        @Valid @ModelAttribute TransactionReportFilter filter
    ) throws Exception {

        ReportFilePropRes reportFilePropRes = jasperReportUseCase.generateTransactionReport(filter);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(reportFilePropRes.fileName()).build());

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(reportFilePropRes.mediaType())
                .body((reportFilePropRes.fileBytes()));
    }
}
