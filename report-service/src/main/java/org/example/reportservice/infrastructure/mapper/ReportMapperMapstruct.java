package org.example.reportservice.infrastructure.mapper;

import org.example.reportservice.application.criteria.AccountSearchCriteria;
import org.example.reportservice.infrastructure.user.dto.AccountReportFilter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface ReportMapperMapstruct {

    @Mapping(target = "createdFrom", source = "createdFrom", qualifiedByName = "localDateToInstantStartOfDay")
    @Mapping(target = "createdTo", source = "createdTo", qualifiedByName = "localDateToInstantEndOfDay")
    AccountSearchCriteria toCriteria(AccountReportFilter filter);

    @Named("localDateToInstantStartOfDay")
    default Instant localDateToInstantStartOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay(ZoneId.systemDefault()).toInstant() : null;
    }

    @Named("localDateToInstantEndOfDay")
    default Instant localDateToInstantEndOfDay(LocalDate date) {
        return date != null ? date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusMillis(1) : null;
    }

}
