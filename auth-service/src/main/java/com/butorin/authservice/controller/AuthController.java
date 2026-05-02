package com.butorin.authservice.controller;

import com.butorin.authservice.dto.RegisterRequest;
import com.butorin.authservice.exception.DuplicateEmailException;
import com.butorin.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Map<String, String> register(@Valid @RequestBody RegisterRequest request) {
        String result = authService.register(request.getUsername(), request.getEmail(), request.getPassword());
        return Map.of("message", result);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestParam String username, @RequestParam String password) {
        String token = authService.login(username, password);
        return Map.of("access_token", token);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEmail(DuplicateEmailException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", exception.getMessage()));
    }
}
