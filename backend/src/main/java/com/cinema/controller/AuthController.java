package com.cinema.controller;

import com.cinema.dto.*;
import com.cinema.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/auth/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/auth/me")
    public ApiResponse<Map<String, String>> me(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new IllegalArgumentException("请先登录");
        }
        var user = authService.findByUsername(auth.getName());
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return ApiResponse.ok(Map.of("username", user.getUsername(), "role", user.getRole()));
    }
}
