package com.butorin.authservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component
@ConfigurationProperties(prefix = "keycloak")
@Data
public class KeycloakProperties {
    private String authServerUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
}
