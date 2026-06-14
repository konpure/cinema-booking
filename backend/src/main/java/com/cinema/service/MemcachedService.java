package com.cinema.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MemcachedService {

    private MemcachedClient client;

    @Value("${memcached.host:127.0.0.1}")
    private String host;

    @Value("${memcached.port:11211}")
    private int port;

    @PostConstruct
    public void init() {
        try {
            client = new XMemcachedClientBuilder(host + ":" + port).build();
            log.info("Memcached connected to {}:{}", host, port);
        } catch (Exception e) {
            log.warn("Memcached unavailable: {}", e.getMessage());
        }
    }

    public void set(String key, int ttl, String value) {
        try { if (client != null) client.set(key, ttl, value); }
        catch (Exception e) { log.warn("Memcached set error: {}", e.getMessage()); }
    }

    public String get(String key) {
        try {
            if (client == null) return null;
            Object v = client.get(key);
            return v instanceof String ? (String) v : null;
        } catch (Exception e) { return null; }
    }

    public void delete(String key) {
        try { if (client != null) client.delete(key); }
        catch (Exception e) { /* ignore */ }
    }

    @PreDestroy
    public void shutdown() {
        try { if (client != null) client.shutdown(); } catch (Exception ignored) {}
    }
}
