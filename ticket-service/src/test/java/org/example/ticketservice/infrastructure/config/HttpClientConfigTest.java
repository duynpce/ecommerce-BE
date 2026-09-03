package org.example.ticketservice.infrastructure.config;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HttpClientConfigTest {

    private final HttpClientConfig config = new HttpClientConfig();

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void resolvesAccessTokenCookieFirst() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("accessToken", "cookie-token"));
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer header-token");
        bind(request);

        assertEquals("cookie-token", config.resolveAccessTokenFromRequest());
    }

    @Test
    void fallsBackToBearerHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer header-token");
        bind(request);

        assertEquals("header-token", config.resolveAccessTokenFromRequest());
    }

    @Test
    void backgroundExecutionHasNoUserToken() {
        assertNull(config.resolveAccessTokenFromRequest());
    }

    private void bind(MockHttpServletRequest request) {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
