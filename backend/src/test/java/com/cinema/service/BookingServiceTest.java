package com.cinema.service;

import com.cinema.dto.LockSeatsRequest;
import com.cinema.dto.SeatMapResponse;
import com.cinema.dto.SeatPosition;
import com.cinema.dto.SnackOrderItem;
import com.cinema.entity.Order;
import com.cinema.entity.Screening;
import com.cinema.mapper.OrderSeatMapper;
import com.cinema.mapper.ScreeningMapper;
import com.cinema.mq.OrderMessage;
import com.cinema.mq.OrderNotification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private ScreeningMapper screeningMapper;

    @Mock
    private OrderSeatMapper orderSeatMapper;

    @Mock
    private RedisSeatService redisSeatService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private BookingService bookingService;

    // ==================== getSeatMap ====================

    @Test
    void getSeatMapReturnsCorrectFormat() {
        Screening screening = new Screening();
        screening.setId(1L);
        screening.setSeatRows(8);
        screening.setSeatCols(12);

        when(screeningMapper.selectById(1L)).thenReturn(screening);
        when(orderSeatMapper.findSoldSeats(1L)).thenReturn(List.of("1-1", "1-2"));
        when(redisSeatService.getLockedSeatsByUser(eq(1L), any()))
                .thenReturn(Map.of("locked", List.of("2-1"), "myLocked", List.of()));

        SeatMapResponse response = bookingService.getSeatMap(1L, 100L);

        assertEquals(1L, response.getScreeningId());
        assertEquals(8, response.getRows());
        assertEquals(12, response.getCols());
        assertEquals(2, response.getSold().size());
        assertEquals(1, response.getLocked().size());
    }

    @Test
    void getSeatMapThrowsWhenScreeningNotFound() {
        when(screeningMapper.selectById(999L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.getSeatMap(999L, 100L));
    }

    // ==================== lockSeats ====================

    @Test
    void lockSeatsSuccess() {
        Screening screening = new Screening();
        screening.setId(1L);
        screening.setSeatRows(8);
        screening.setSeatCols(12);
        screening.setStatus("OPEN");

        LockSeatsRequest request = new LockSeatsRequest();
        request.setScreeningId(1L);
        request.setSeats(List.of(new SeatPosition(1, 2), new SeatPosition(1, 3)));

        when(screeningMapper.selectById(1L)).thenReturn(screening);
        when(orderSeatMapper.findSoldSeats(1L)).thenReturn(List.of());
        when(redisSeatService.lockSeats(eq(1L), eq(100L), anyList(), anyList()))
                .thenReturn("lock-token-123");

        Map<String, String> result = bookingService.lockSeats(100L, request);

        assertNotNull(result.get("lockToken"));
        assertEquals("lock-token-123", result.get("lockToken"));
        assertTrue(result.get("message").contains("10 分钟"));
    }

    @Test
    void lockSeatsFailsWhenSeatOutOfRange() {
        Screening screening = new Screening();
        screening.setId(1L);
        screening.setSeatRows(8);
        screening.setSeatCols(12);
        screening.setStatus("OPEN");

        LockSeatsRequest request = new LockSeatsRequest();
        request.setScreeningId(1L);
        request.setSeats(List.of(new SeatPosition(10, 15)));

        when(screeningMapper.selectById(1L)).thenReturn(screening);

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.lockSeats(100L, request));
    }

    @Test
    void lockSeatsFailsWhenScreeningClosed() {
        Screening screening = new Screening();
        screening.setId(1L);
        screening.setSeatRows(8);
        screening.setSeatCols(12);
        screening.setStatus("CLOSED");

        LockSeatsRequest request = new LockSeatsRequest();
        request.setScreeningId(1L);
        request.setSeats(List.of(new SeatPosition(1, 2)));

        when(screeningMapper.selectById(1L)).thenReturn(screening);

        assertThrows(IllegalStateException.class,
                () -> bookingService.lockSeats(100L, request));
    }

    // ==================== submitOrder ====================

    @Test
    void submitOrderSuccess() {
        Screening screening = new Screening();
        screening.setId(1L);
        screening.setCinemaId(1L);
        screening.setSeatRows(8);
        screening.setSeatCols(12);
        screening.setStatus("OPEN");

        LockSeatsRequest request = new LockSeatsRequest();
        request.setScreeningId(1L);
        request.setSeats(List.of(new SeatPosition(1, 2)));

        Order order = new Order();
        order.setOrderNo("C123-100");
        order.setUserId(100L);
        order.setScreeningId(1L);

        when(screeningMapper.selectById(1L)).thenReturn(screening);
        when(redisSeatService.validateToken("token-123", 100L, 1L)).thenReturn(true);
        when(orderService.createOrderFromMessage(any(OrderMessage.class))).thenReturn(order);

        Map<String, String> result = bookingService.submitOrder(100L, request, "token-123", List.of());

        assertEquals("C123-100", result.get("orderNo"));
        assertEquals("购票成功", result.get("message"));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(OrderNotification.class));
    }

    @Test
    void submitOrderFailsWhenTokenExpired() {
        Screening screening = new Screening();
        screening.setId(1L);
        screening.setSeatRows(8);
        screening.setSeatCols(12);
        screening.setStatus("OPEN");

        LockSeatsRequest request = new LockSeatsRequest();
        request.setScreeningId(1L);
        request.setSeats(List.of(new SeatPosition(1, 2)));

        when(screeningMapper.selectById(1L)).thenReturn(screening);
        when(redisSeatService.validateToken("expired-token", 100L, 1L)).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> bookingService.submitOrder(100L, request, "expired-token", List.of()));
    }

    @Test
    void submitOrderReleasesLockOnOrderCreationFailure() {
        Screening screening = new Screening();
        screening.setId(1L);
        screening.setCinemaId(1L);
        screening.setSeatRows(8);
        screening.setSeatCols(12);
        screening.setStatus("OPEN");

        LockSeatsRequest request = new LockSeatsRequest();
        request.setScreeningId(1L);
        request.setSeats(List.of(new SeatPosition(1, 2)));

        when(screeningMapper.selectById(1L)).thenReturn(screening);
        when(redisSeatService.validateToken("token-456", 100L, 1L)).thenReturn(true);
        when(orderService.createOrderFromMessage(any(OrderMessage.class)))
                .thenThrow(new RuntimeException("创建订单失败"));

        assertThrows(RuntimeException.class,
                () -> bookingService.submitOrder(100L, request, "token-456", List.of()));
        verify(redisSeatService).releaseByToken(eq("token-456"), anyList(), eq(1L));
    }
}
