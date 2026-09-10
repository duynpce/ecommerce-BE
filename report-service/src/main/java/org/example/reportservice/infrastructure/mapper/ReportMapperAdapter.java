package org.example.reportservice.infrastructure.mapper;

import lombok.RequiredArgsConstructor;
import org.example.reportservice.application.criteria.AccountSearchCriteria;
import org.example.reportservice.application.mapper.ReportMapper;
import org.example.reportservice.infrastructure.user.dto.AccountReportFilter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportMapperAdapter implements ReportMapper {
    private final ReportMapperMapstruct reportMapperMapstruct;

    @Override
    public AccountSearchCriteria toAccountSearchCriteria(AccountReportFilter filter) {
        return reportMapperMapstruct.toCriteria(filter);
    }
}
