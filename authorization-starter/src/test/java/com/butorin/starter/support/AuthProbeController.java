package com.butorin.starter.support;

import com.butorin.starter.annotation.Auth;
import com.butorin.starter.dto.AuthorizationParams;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthProbeController {

    @GetMapping("/probe/auth")
    AuthorizationParams probe(@Auth AuthorizationParams params) {
        return params;
    }
}
