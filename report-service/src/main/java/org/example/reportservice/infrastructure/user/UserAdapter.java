package org.example.reportservice.infrastructure.user;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.reportservice.application.client.UserClient;
import org.example.reportservice.infrastructure.user.httpclient.UserHttpClient;
import org.example.reportservice.infrastructure.user.dto.AccountReportFilter;
import org.example.reportservice.infrastructure.user.dto.AccountReportResponsive;
import org.example.reportservice.infrastructure.web.dto.ResponseDto;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserAdapter implements UserClient {

    private final UserHttpClient userHttpClient;

    @Override
    public List<AccountReportResponsive> getAccountReport(AccountReportFilter filter) {
        ResponseDto<List<AccountReportResponsive>> response = userHttpClient.getAccountReport(
                filter.page(),
                filter.limit(),
                filter.firstName(),
                filter.lastName(),
                filter.gender(),
                filter.phoneNumber(),
                filter.createdFrom(),
                filter.createdTo());

        if (response == null || response.getData() == null) {
            return List.of();
        }
        return response.getData();
    }
}
