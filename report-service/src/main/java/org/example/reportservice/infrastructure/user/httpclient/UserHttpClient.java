package org.example.reportservice.infrastructure.user.httpclient;

import java.time.LocalDate;
import java.util.List;
import org.example.reportservice.domain.constant.Gender;
import org.example.reportservice.infrastructure.web.dto.AccountReportResponsive;
import org.example.reportservice.infrastructure.web.dto.ResponseDto;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface UserHttpClient {

    @GetExchange("/api/v1/users/account-profiles/report")
    ResponseDto<List<AccountReportResponsive>> getAccountReport(
            @RequestParam Integer page,
            @RequestParam Integer limit,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) LocalDate createdFrom,
            @RequestParam(required = false) LocalDate createdTo);
}
