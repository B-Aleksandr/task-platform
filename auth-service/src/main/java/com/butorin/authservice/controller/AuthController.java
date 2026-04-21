package com.butorin.authservice.controller;

import com.butorin.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password) {
        return authService.register(username, password);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestParam String username, @RequestParam String password) {
        String token = authService.login(username, password);
        return Map.of("access_token", token);
    }
}
