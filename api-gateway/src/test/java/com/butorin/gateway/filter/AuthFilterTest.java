package com.butorin.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthFilterTest {

    private AuthFilter authFilter;

    @Mock
    private WebClient.Builder webClientBuilder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private GatewayFilterChain chain;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @BeforeEach
    void setUp() {
        authFilter = new AuthFilter(webClientBuilder, objectMapper);
    }

    @Test
    void registerPath_skipsTokenValidation() {
        when(exchange.getRequest()).thenReturn(request);
        when(request.getURI()).thenReturn(URI.create("http://localhost/auth/register"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(authFilter.filter(exchange, chain)).verifyComplete();
        verify(chain).filter(exchange);
    }

    @Test
    void loginPath_skipsTokenValidation() {
        when(exchange.getRequest()).thenReturn(request);
        when(request.getURI()).thenReturn(URI.create("http://localhost/auth/login"));
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(authFilter.filter(exchange, chain)).verifyComplete();
        verify(chain).filter(exchange);
    }

    @Test
    void protectedPath_withoutBearer_returnsUnauthorized() {
        when(exchange.getRequest()).thenReturn(request);
        when(request.getURI()).thenReturn(URI.create("http://localhost/tasks/1"));
        HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);
        when(exchange.getResponse()).thenReturn(response);
        when(response.setComplete()).thenReturn(Mono.empty());

        StepVerifier.create(authFilter.filter(exchange, chain)).verifyComplete();

        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }
}
