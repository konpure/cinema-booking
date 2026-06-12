package com.cinema.controller;

import com.cinema.dto.ApiResponse;
import com.cinema.zookeeper.ZooKeeperRegistryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/zookeeper")
@RequiredArgsConstructor
public class ZooKeeperController {

    private final ZooKeeperRegistryService registryService;

    @GetMapping("/instances")
    public ApiResponse<Map<String, Object>> instances() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", registryService.status());
        result.put("instances", registryService.listInstances());
        return ApiResponse.ok(result);
    }
}
