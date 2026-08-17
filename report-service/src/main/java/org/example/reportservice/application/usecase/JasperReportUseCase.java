package org.example.reportservice.application.usecase;

import net.sf.jasperreports.engine.JRException;
import org.example.reportservice.infrastructure.product.dto.TransactionReportFilter;
import org.example.reportservice.infrastructure.user.dto.AccountReportFilter;
import org.example.reportservice.infrastructure.web.dto.ReportFilePropRes;

public interface JasperReportUseCase {

    ReportFilePropRes generateAccountReport(AccountReportFilter filter) throws JRException;
    ReportFilePropRes generateTransactionReport(TransactionReportFilter filter) throws JRException;
}