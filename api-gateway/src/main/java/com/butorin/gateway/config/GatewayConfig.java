package com.butorin.gateway.config;

import com.butorin.gateway.filter.AuthFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final AuthFilter authFilter;

    public GatewayConfig(AuthFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/auth/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://localhost:8081"))
                .route("task-service", r -> r
                        .path("/tasks/**")
                        .filters(f -> f.filter(authFilter))
                        .uri("http://localhost:8082"))
                .build();
    }
}
