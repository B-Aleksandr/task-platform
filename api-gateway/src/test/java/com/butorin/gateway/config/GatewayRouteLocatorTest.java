package com.butorin.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Проверка маршрутов из {@link GatewayConfig}: {@code /auth/**} и {@code /tasks/**}.
 * Маршрута {@code /balances/**} в текущей конфигурации API Gateway нет.
 */
@SpringBootTest
class GatewayRouteLocatorTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void exposesAuthAndTaskServiceRoutes() {
        List<Route> routes = routeLocator.getRoutes()
                .collectList()
                .block(Duration.ofSeconds(15));

        assertThat(routes).extracting(Route::getId).contains("auth-service", "task-service");
    }
}
