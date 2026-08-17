package org.example.reportservice.application.mapper;


import org.example.reportservice.application.criteria.AccountSearchCriteria;
import org.example.reportservice.infrastructure.user.dto.AccountReportFilter;

public interface ReportMapper {

    AccountSearchCriteria toAccountSearchCriteria(AccountReportFilter filter);

}
