package com.butorin.starter.resolver;

import com.butorin.starter.annotation.Auth;
import com.butorin.starter.config.AuthAutoConfiguration;
import com.butorin.starter.dto.AuthorizationParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(classes = {
        AuthArgumentResolverIntegrationTest.TestApplication.class,
        AuthArgumentResolverIntegrationTest.TestController.class
})
@AutoConfigureMockMvc
class AuthArgumentResolverIntegrationTest {

    private static final String REALM_NAME = "test-realm";
    private static final String ADMIN_REALM = "master";
    private static final String ADMIN_CLIENT_ID = "admin-cli";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    @Container
    static final GenericContainer<?> KEYCLOAK = new GenericContainer<>("quay.io/keycloak/keycloak:26.1")
            .withExposedPorts(8080)
            .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "admin")
            .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "admin")
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("keycloak/test-realm.json"),
                    "/opt/keycloak/data/import/test-realm.json"
            )
            .withCommand("start-dev --import-realm")
            .waitingFor(Wait.forHttp("/realms/" + REALM_NAME + "/.well-known/openid-configuration")
                    .forStatusCode(200))
            .withStartupTimeout(Duration.ofMinutes(2));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void resolvesAuthorizationParamsFromKeycloakTokenClaims() throws Exception {
        String accessToken = obtainAccessToken();
        String subject = parseSubject(accessToken);
        List<String> roles = List.of("admin", "manage-account");
        String rolesHeader = roles.stream()
                .map(role -> "\"" + role + "\"")
                .reduce((left, right) -> left + "," + right)
                .map(value -> "[" + value + "]")
                .orElse("[]");

        mockMvc.perform(get("/auth-check")
                        .header("user-id", subject)
                        .header("user-roles", rolesHeader)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(subject))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles", containsInAnyOrder(roles.toArray())));
    }

    private String obtainAccessToken() throws Exception {
        String body = "grant_type=password"
                + "&client_id=" + encode(ADMIN_CLIENT_ID)
                + "&username=" + encode(ADMIN_USERNAME)
                + "&password=" + encode(ADMIN_PASSWORD);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(keycloakUrl() + "/realms/" + ADMIN_REALM + "/protocol/openid-connect/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Token request failed: " + response.statusCode() + " -> " + response.body());
        }

        JsonNode tokenResponse = objectMapper.readTree(response.body());
        return tokenResponse.path("access_token").asText();
    }

    private String parseSubject(String jwt) throws Exception {
        String[] parts = jwt.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        JsonNode claims = objectMapper.readTree(payload);
        return claims.path("sub").asText();
    }

    private String keycloakUrl() {
        return "http://" + KEYCLOAK.getHost() + ":" + KEYCLOAK.getMappedPort(8080);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(AuthAutoConfiguration.class)
    static class TestApplication {
    }

    @RestController
    static class TestController {

        @GetMapping("/auth-check")
        AuthorizationParams authCheck(@Auth AuthorizationParams authorizationParams) {
            return authorizationParams;
        }
    }
}
