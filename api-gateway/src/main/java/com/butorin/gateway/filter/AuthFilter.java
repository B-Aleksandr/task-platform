package com.butorin.gateway.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GatewayFilter, Ordered {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private static final String CLIENT_SECRET = "ygouU7hhltEpoyIk4gNW2jrZZnWDpIRO";

    public AuthFilter(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String path = exchange.getRequest().getURI().getPath();

        System.out.println("=== Gateway Filter ===");
        System.out.println("Path: " + path);

        if (path.startsWith("/auth/register") || path.startsWith("/auth/login")) {
            System.out.println("Пропускаем запрос к auth-service без проверки токена");
            return chain.filter(exchange);
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Токен отсутствует или неправильный формат");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String userToken = authHeader.substring(7);
        System.out.println("Пользовательский токен получен");

        return validateTokenAndForward(userToken, exchange, chain);
    }

    private Mono<String> getServiceToken() {
        WebClient webClient = webClientBuilder.build();

        return webClient.post()
                .uri("http://localhost:8080/realms/task-realm/protocol/openid-connect/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("client_id=gateway-client&client_secret=" + CLIENT_SECRET + "&grant_type=client_credentials")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        JsonNode json = objectMapper.readTree(response);
                        return json.get("access_token").asText();
                    } catch (Exception e) {
                        throw new RuntimeException("Ошибка парсинга service-токена", e);
                    }
                });
    }

    private Mono<Void> validateTokenAndForward(String userToken, ServerWebExchange exchange, GatewayFilterChain chain) {
        return getServiceToken()
                .flatMap(serviceToken -> {
                    System.out.println("Service-токен получен, вызываем /userinfo");

                    WebClient webClient = webClientBuilder.build();
                    return webClient.get()
                            .uri("http://localhost:8080/realms/task-realm/protocol/openid-connect/userinfo")
                            .header("Authorization", "Bearer " + serviceToken)
                            .retrieve()
                            .onStatus(status -> status.value() == 401, response -> {
                                System.out.println("Keycloak вернул 401");
                                return Mono.error(new RuntimeException("Invalid service token"));
                            })
                            .bodyToMono(String.class)
                            .flatMap(userInfo -> {
                                try {
                                    JsonNode json = objectMapper.readTree(userInfo);
                                    String userId = json.get("sub").asText();
                                    String roles = "";
                                    if (json.has("realm_access") && json.get("realm_access").has("roles")) {
                                        roles = json.get("realm_access").get("roles").toString();
                                    }

                                    System.out.println("userId: " + userId + ", roles: " + roles);

                                    ServerWebExchange mutatedExchange = exchange.mutate()
                                            .request(exchange.getRequest().mutate()
                                                    .header("user-id", userId)
                                                    .header("user-roles", roles)
                                                    .build())
                                            .build();

                                    return chain.filter(mutatedExchange);
                                } catch (Exception e) {
                                    System.err.println("Ошибка парсинга: " + e.getMessage());
                                    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                                    return exchange.getResponse().setComplete();
                                }
                            });
                })
                .onErrorResume(e -> {
                    System.err.println("Ошибка: " + e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
