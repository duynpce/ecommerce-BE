package org.example.reportservice.application.client;

import org.example.reportservice.infrastructure.user.dto.AccountReportFilter;
import org.example.reportservice.infrastructure.user.dto.AccountReportResponsive;

import java.util.List;

public interface UserClient {
    List<AccountReportResponsive> getAccountReport(AccountReportFilter filter);
}
