package com.cinema.zookeeper;

import com.cinema.config.ZooKeeperProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZooKeeperRegistryService {

    private final ZooKeeperProperties properties;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name:cinema-booking}")
    private String instanceId;

    @Value("${server.port:8080}")
    private int serverPort;

    private CuratorFramework client;
    private String registeredPath;
    private volatile boolean connected;

    @PostConstruct
    public void register() {
        if (!properties.isEnabled()) {
            log.info("ZooKeeper registration disabled");
            return;
        }
        client = CuratorFrameworkFactory.builder()
                .connectString(properties.getConnectString())
                .sessionTimeoutMs(properties.getSessionTimeoutMs())
                .connectionTimeoutMs(properties.getConnectionTimeoutMs())
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
        client.start();
        try {
            if (!client.blockUntilConnected(properties.getConnectionTimeoutMs(), TimeUnit.MILLISECONDS)) {
                log.warn("ZooKeeper unavailable at {}, skip service registration", properties.getConnectString());
                return;
            }
            connected = true;
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("instanceId", instanceId);
            payload.put("host", InetAddress.getLocalHost().getHostAddress());
            payload.put("port", serverPort);
            payload.put("registeredAt", Instant.now().toString());
            byte[] data = objectMapper.writeValueAsBytes(payload);
            registeredPath = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(properties.getBasePath() + "/" + instanceId, data);
            log.info("Registered backend instance in ZooKeeper: {}", registeredPath);
        } catch (Exception e) {
            log.warn("Failed to register instance in ZooKeeper: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void deregister() {
        if (client != null) {
            client.close();
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("enabled", properties.isEnabled());
        status.put("connected", connected);
        status.put("connectString", properties.getConnectString());
        status.put("registeredPath", registeredPath);
        status.put("instanceId", instanceId);
        return status;
    }

    public List<Map<String, Object>> listInstances() {
        if (!connected || client == null) {
            return List.of();
        }
        try {
            List<String> children = client.getChildren().forPath(properties.getBasePath());
            List<Map<String, Object>> instances = new ArrayList<>();
            for (String child : children) {
                String path = properties.getBasePath() + "/" + child;
                byte[] raw = client.getData().forPath(path);
                Map<String, Object> info = objectMapper.readValue(raw, new TypeReference<>() {});
                info.put("node", child);
                info.put("path", path);
                instances.add(info);
            }
            return instances;
        } catch (Exception e) {
            log.warn("Failed to list ZooKeeper instances: {}", e.getMessage());
            return List.of();
        }
    }
}
