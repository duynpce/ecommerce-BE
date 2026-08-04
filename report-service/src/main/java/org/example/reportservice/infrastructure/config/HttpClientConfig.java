package org.example.reportservice.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

import io.netty.channel.ChannelOption;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.example.reportservice.domain.exception.ExternalServiceException;
import org.example.reportservice.infrastructure.product.httpclient.ProductHttpClient;
import org.example.reportservice.infrastructure.prop.AppProperties;
import org.example.reportservice.infrastructure.user.httpclient.UserHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.Arrays;

@Configuration
public class HttpClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(10);
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";


    @Bean
    public ProductHttpClient getProductHttpClient(AppProperties appProperties, UserHttpClient userHttpClient) {
        return createHttpClient(appProperties.getProductServiceUrl(), ProductHttpClient.class);
    }


    @Bean
    public UserHttpClient userHttpClient(AppProperties props) {
        return createHttpClient(props.getUserServiceUrl(), UserHttpClient.class);
    }

    private <T> T createHttpClient(String baseUrl, Class<T> clientClass) {
        WebClient webClient = buildWebClient(baseUrl);
        return HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build()
                .createClient(clientClass);
    }

    private WebClient buildWebClient(String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) CONNECT_TIMEOUT.toMillis())
                .responseTimeout(RESPONSE_TIMEOUT);

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                // ── Forward the httpOnly accessToken cookie to downstream service ─────
                .filter((request, next) -> {
                    String token = resolveAccessTokenFromCookie();
                    if (token == null) {
                        return next.exchange(request);
                    }
                    return next.exchange(
                            org.springframework.web.reactive.function.client.ClientRequest
                                    .from(request)
                                    // Forward as cookie — downstream SecurityConfig reads it
                                    // via the custom ServerAuthenticationConverter
                                    .cookie(ACCESS_TOKEN_COOKIE, token)
                                    .build()
                    );
                })
                // ─────────────────────────────────────────────────────────────────────
                .defaultStatusHandler(HttpStatusCode::isError, this::handleErrorResponse)
                .build();
    }

    /**
     * Reads the accessToken from the current servlet request's cookies.
     * Returns null if not present or not in a servlet context (e.g. scheduler).
     */
    private String resolveAccessTokenFromCookie() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(c -> ACCESS_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private Mono<? extends Throwable> handleErrorResponse(ClientResponse clientResponse) {
        HttpStatus status = Optional.ofNullable(HttpStatus.resolve(clientResponse.statusCode().value()))
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);

        return clientResponse.bodyToMono(String.class)
                .defaultIfEmpty(status.getReasonPhrase())
                .flatMap(errorBody -> {
                    String message = extractMessage(errorBody);
                    return Mono.error(new ExternalServiceException(message, status));
                });
    }

    private String extractMessage(String errorBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(errorBody);
            JsonNode messageNode = root.path("message");
            return messageNode.isMissingNode() ? errorBody : messageNode.asText();
        } catch (Exception e) {
            return errorBody;
        }
    }
}