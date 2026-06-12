package com.cinema.controller;

import com.cinema.dto.ApiResponse;
import com.cinema.zookeeper.ZooKeeperRegistryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final ZooKeeperRegistryService zooKeeperRegistryService;

    @GetMapping("/api/health")
    public ApiResponse<Map<String, Object>> health(
            @Value("${spring.application.name:cinema-booking}") String instanceId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("instance", instanceId);
        body.put("zookeeper", zooKeeperRegistryService.status());
        return ApiResponse.ok(body);
    }
}
