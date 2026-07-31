package org.example.productservice.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import org.example.productservice.domain.exception.ExternalServiceException;
import org.example.productservice.infrastructure.prop.AppProperties;
import org.example.productservice.infrastructure.ticket.httpclient.TicketHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Optional;

@Configuration
public class HttpClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(10);

    @Bean
    public TicketHttpClient ticketHttpClient(AppProperties props) {
        return createHttpClient(props.getTicketServiceUrl(), TicketHttpClient.class);
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
                .defaultStatusHandler(HttpStatusCode::isError, this::handleErrorResponse)
                .build();
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
