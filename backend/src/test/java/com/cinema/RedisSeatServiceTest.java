package com.cinema;

import com.cinema.dto.SeatPosition;
import com.cinema.service.RedisSeatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class RedisSeatServiceTest {

    @MockBean
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisSeatService redisSeatService;

    @Test
    void seatKeyFormatsCorrectly() {
        assertEquals("3-5", redisSeatService.seatKey(3, 5));
    }

    @Test
    void lockSeatsSuccessWhenAllAvailable() {
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);

        String token = redisSeatService.lockSeats(1L, 100L,
                List.of(new SeatPosition(1, 2), new SeatPosition(1, 3)), List.of());

        assertNotNull(token);
        verify(ops, times(3)).setIfAbsent(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void lockSeatsFailsWhenSeatSold() {
        assertThrows(IllegalStateException.class, () ->
                redisSeatService.lockSeats(1L, 100L,
                        List.of(new SeatPosition(2, 4)), List.of("2-4")));
    }
}
