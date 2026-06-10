package com.cinema.service;

import com.cinema.dto.SeatPosition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSeatService {

    private final StringRedisTemplate redisTemplate;

    @Value("${cinema.seat-lock-ttl-seconds:600}")
    private int lockTtlSeconds;

    private String lockKey(Long screeningId, int row, int col) {
        return "cinema:lock:" + screeningId + ":" + row + "-" + col;
    }

    private String tokenKey(String token) {
        return "cinema:locktoken:" + token;
    }

    public String seatKey(int row, int col) {
        return row + "-" + col;
    }

    public Map<String, List<String>> getLockedSeatsByUser(Long screeningId, Long currentUserId) {
        String pattern = "cinema:lock:" + screeningId + ":*";
        var keys = redisTemplate.keys(pattern);
        List<String> others = new ArrayList<>();
        List<String> mine = new ArrayList<>();
        if (keys == null) {
            return Map.of("locked", others, "myLocked", mine);
        }
        String prefix = "cinema:lock:" + screeningId + ":";
        for (String key : keys) {
            String seat = key.substring(prefix.length());
            String owner = redisTemplate.opsForValue().get(key);
            if (currentUserId != null && currentUserId.toString().equals(owner)) {
                mine.add(seat);
            } else {
                others.add(seat);
            }
        }
        Map<String, List<String>> result = new HashMap<>();
        result.put("locked", others);
        result.put("myLocked", mine);
        return result;
    }

    public String lockSeats(Long screeningId, Long userId, List<SeatPosition> seats, List<String> soldSeats) {
        String token = UUID.randomUUID().toString();
        List<String> acquired = new ArrayList<>();

        try {
            for (SeatPosition seat : seats) {
                String key = seatKey(seat.getRow(), seat.getCol());
                if (soldSeats.contains(key)) {
                    throw new IllegalStateException("座位 " + key + " 已售出");
                }
                String lockKey = lockKey(screeningId, seat.getRow(), seat.getCol());
                Boolean ok = redisTemplate.opsForValue().setIfAbsent(lockKey, userId.toString(),
                        Duration.ofSeconds(lockTtlSeconds));
                if (Boolean.FALSE.equals(ok)) {
                    throw new IllegalStateException("座位 " + key + " 已被占用");
                }
                acquired.add(lockKey);
            }
            redisTemplate.opsForValue().set(tokenKey(token), screeningId + ":" + userId,
                    Duration.ofSeconds(lockTtlSeconds));
            log.info("Redis locked {} seats for screening {} user {}", seats.size(), screeningId, userId);
            return token;
        } catch (RuntimeException e) {
            acquired.forEach(k -> redisTemplate.delete(k));
            throw e;
        }
    }

    public void releaseByToken(String token, List<SeatPosition> seats, Long screeningId) {
        for (SeatPosition seat : seats) {
            redisTemplate.delete(lockKey(screeningId, seat.getRow(), seat.getCol()));
        }
        redisTemplate.delete(tokenKey(token));
    }

    public boolean validateToken(String token, Long userId, Long screeningId) {
        String val = redisTemplate.opsForValue().get(tokenKey(token));
        if (val == null) return false;
        String[] parts = val.split(":");
        return parts.length == 2 && parts[0].equals(screeningId.toString()) && parts[1].equals(userId.toString());
    }
}
