package org.example.reportservice.infrastructure.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * Bound to app.user-service-url. Scheme+host+port only (no path) -
     * UserHttpClient's @GetExchange paths already include /api/v1/users/...
     */
    private String userServiceUrl;
    private String productServiceUrl;
}
