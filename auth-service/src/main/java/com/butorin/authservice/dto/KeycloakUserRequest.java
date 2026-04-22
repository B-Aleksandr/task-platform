package com.butorin.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeycloakUserRequest {
    private String username;
    private boolean enabled;
    private String firstName;
    private String lastName;
    private String email;
    private List<Credential> credentials;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Credential {
        private String type;
        private String value;
        private boolean temporary;
    }
}
