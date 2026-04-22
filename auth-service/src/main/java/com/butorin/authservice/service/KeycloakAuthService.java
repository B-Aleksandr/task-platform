package com.butorin.authservice.service;

import com.butorin.authservice.config.KeycloakProperties;
import com.butorin.authservice.dto.KeycloakUserRequest;
import com.butorin.authservice.dto.TokenResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakAuthService {

    private final KeycloakProperties keycloakProperties;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

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

        KeycloakUserRequest userRequest = new KeycloakUserRequest();
        userRequest.setUsername(username);
        userRequest.setEnabled(true);
        userRequest.setFirstName("User");
        userRequest.setLastName("Test");
        userRequest.setEmail(username + "@test.com");
        userRequest.setCredentials(List.of(
                new KeycloakUserRequest.Credential("password", password, false)
        ));

        try {
            String userJson = objectMapper.writeValueAsString(userRequest);

            webClient.post()
                    .uri(keycloakProperties.getAuthServerUrl() + "/admin/realms/" + keycloakProperties.getRealm() + "/users")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + adminToken)
                    .bodyValue(userJson)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации пользователя", e);
        }
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
