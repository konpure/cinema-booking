package com.cinema.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cinema.zookeeper")
public class ZooKeeperProperties {

    private boolean enabled = true;
    private String connectString = "localhost:2181";
    private String basePath = "/cinema/instances";
    private int sessionTimeoutMs = 5000;
    private int connectionTimeoutMs = 5000;
}
