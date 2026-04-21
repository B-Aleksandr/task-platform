package com.butorin.authservice.service;

import com.butorin.authservice.config.KeycloakProperties;
import com.butorin.authservice.dto.TokenResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Service
@RequiredArgsConstructor
public class KeycloakAuthService {

    private final KeycloakProperties keycloakProperties;
    private final WebClient.Builder webClientBuilder;

    public String getAdminToken() {
        WebClient webClient = webClientBuilder.build();

        TokenResponse response = webClient.post()
                .uri(keycloakProperties.getAuthServerUrl() + "/realms/master/protocol/openid-connect/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("client_id=admin-cli&username=admin&password=admin&grant_type=password")
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block();

        return response.getAccessToken();
    }

    public void createUser(String username, String password) {
        WebClient webClient = webClientBuilder.build();

        String adminToken = getAdminToken();

        String userJson = String.format(
                "{\"username\":\"%s\",\"enabled\":true,\"firstName\":\"User\",\"lastName\":\"Test\",\"email\":\"%s@test.com\",\"credentials\":[{\"type\":\"password\",\"value\":\"%s\",\"temporary\":false}]}",
                username, username, password
        );

        webClient.post()
                .uri(keycloakProperties.getAuthServerUrl() + "/admin/realms/" + keycloakProperties.getRealm() + "/users")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .bodyValue(userJson)
                .retrieve()
                .toBodilessEntity()
                .block();


    }

    public TokenResponse loginUser(String username, String password) {
        RestTemplate restTemplate = new RestTemplate();

        String url = keycloakProperties.getAuthServerUrl() + "/realms/task-realm/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "client_id=task-client&client_secret=" + keycloakProperties.getClientSecret() +
                "&username=" + username + "&password=" + password + "&grant_type=password";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<TokenResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, TokenResponse.class);

        return response.getBody();
    }

    public String getUserIdByUsername(String username) {
        WebClient webClient = webClientBuilder.build();

        String adminToken = getAdminToken();

        String response = webClient.get()
                .uri(keycloakProperties.getAuthServerUrl() + "/admin/realms/" + keycloakProperties.getRealm() + "/users?username=" + username)
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Парсим JSON
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode array = objectMapper.readTree(response);
            if (array.isArray() && array.size() > 0) {
                return array.get(0).get("id").asText();
            }
        } catch (Exception e) {
            throw new RuntimeException("Не удалось получить ID пользователя", e);
        }

        throw new RuntimeException("Пользователь не найден: " + username);
    }
}
