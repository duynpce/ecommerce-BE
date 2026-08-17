package org.example.reportservice.application.service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.pdf.JRPdfExporter;
import org.example.reportservice.application.client.ProductClient;
import org.example.reportservice.application.client.UserClient;
import org.example.reportservice.application.usecase.JasperReportUseCase;
import org.example.reportservice.infrastructure.product.dto.TransactionReportFilter;
import org.example.reportservice.infrastructure.product.dto.TransactionReportResponse;
import org.example.reportservice.infrastructure.user.dto.AccountReportFilter;
import org.example.reportservice.infrastructure.user.dto.AccountReportResponsive;
import org.example.reportservice.infrastructure.web.dto.ReportFilePropRes;
import org.example.reportservice.infrastructure.web.dto.ResponseDto;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JasperReportService implements JasperReportUseCase {

    private static final String ACCOUNT_REPORT_PATH     = "report/account_report.jasper";
    private static final String TRANSACTION_REPORT_PATH = "report/transaction_report.jasper";

    private static final MediaType MEDIA_XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final UserClient    userClient;
    private final ProductClient productClient;

    private JasperReport accountReport;
    private JasperReport transactionReport;

    // -------------------------------------------------------------------------
    // Startup
    // -------------------------------------------------------------------------

    @PostConstruct
    void loadCompiledReports() throws JRException {
        accountReport     = loadReport(ACCOUNT_REPORT_PATH);
        transactionReport = loadReport(TRANSACTION_REPORT_PATH);
    }

    private JasperReport loadReport(String classpathLocation) throws JRException {
        try (InputStream is = new ClassPathResource(classpathLocation).getInputStream()) {
            return (JasperReport) JRLoader.loadObject(is);
        } catch (IOException e) {
            throw new JRException("Error loading compiled report: " + classpathLocation, e);
        }
    }

    // -------------------------------------------------------------------------
    // Use-case implementations
    // -------------------------------------------------------------------------

    @Override
    public ReportFilePropRes generateAccountReport(AccountReportFilter filter) throws JRException {
        List<AccountReportResponsive> data = userClient.getAccountReport(filter);

        Map<String, Object> params = new HashMap<>();
        params.put("ReportTitle", "Account Report");

        return switch (filter.exportFileName()) {
            case PDF  -> generatePdf(accountReport, data, params, "account_report.pdf");
            case XLSX -> generateXlsx(accountReport, data, params, "account_report.xlsx");
        };
    }

    @Override
    public ReportFilePropRes generateTransactionReport(TransactionReportFilter filter) throws JRException {
        ResponseDto<List<TransactionReportResponse>> data = productClient.searchTransactions(filter);

        Map<String, Object> params = new HashMap<>();
        params.put("ReportTitle", "Transaction Report");
        params.put("page", filter.page() + 1);
        params.put("totalPage", data.getMetaData().getTotalPages());

        // TransactionFilter reuses the same ExportFormat enum — add it there if needed.
        // For now the endpoint always returns PDF; swap to a switch once the filter
        // carries an exportFormat field (mirroring AccountReportFilter).


        return switch (filter.exportFileName()) {
            case PDF  -> generatePdf(transactionReport, data.getData(), params, "transaction_report.pdf");
            case XLSX -> generateXlsx(transactionReport, data.getData(), params, "transaction_report.xlsx");
        };
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    /**
     * Fills {@code template} with {@code data} and {@code params}, then exports to PDF bytes.
     *
     * @param template   pre-loaded, compiled {@link JasperReport}
     * @param data       bean collection used as the data source
     * @param params     report parameters (e.g. title strings, images)
     * @param fileName   suggested download file name
     * @return           {@link ReportFilePropRes} ready to stream back to the client
     */
    private ReportFilePropRes generatePdf(
            JasperReport template,
            List<?> data,
            Map<String, Object> params,
            String fileName) throws JRException {

        JasperPrint print = fill(template, data, params);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
        exporter.exportReport();

        return new ReportFilePropRes(out.toByteArray(), MediaType.APPLICATION_PDF, fileName);
    }

    /**
     * Fills {@code template} with {@code data} and {@code params}, then exports to XLSX bytes.
     *
     * @param template   pre-loaded, compiled {@link JasperReport}
     * @param data       bean collection used as the data source
     * @param params     report parameters (e.g. title strings)
     * @param fileName   suggested download file name
     * @return           {@link ReportFilePropRes} ready to stream back to the client
     */
    private ReportFilePropRes generateXlsx(
            JasperReport template,
            List<?> data,
            Map<String, Object> params,
            String fileName) throws JRException {

        JasperPrint print = fill(template, data, params);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setExporterInput(new SimpleExporterInput(print));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));

        SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
        config.setOnePagePerSheet(false);
        config.setRemoveEmptySpaceBetweenRows(true);
        config.setDetectCellType(true);
        exporter.setConfiguration(config);
        exporter.exportReport();

        return new ReportFilePropRes(out.toByteArray(), MEDIA_XLSX, fileName);
    }

    /**
     * Combines a compiled report, a bean-collection data source, and parameter map
     * into a {@link JasperPrint} ready for export.
     */
    private JasperPrint fill(
            JasperReport template,
            List<?> data,
            Map<String, Object> params) throws JRException {

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);
        return JasperFillManager.fillReport(template, params, dataSource);
    }
}
