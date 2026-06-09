package com.cinema.controller;

import com.cinema.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ApiResponse<Map<String, String>> health(
            @Value("${spring.application.name:cinema-booking}") String instanceId) {
        return ApiResponse.ok(Map.of("status", "UP", "instance", instanceId));
    }
}
