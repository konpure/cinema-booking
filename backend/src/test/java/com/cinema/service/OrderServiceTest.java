package com.cinema.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cinema.dto.SeatPosition;
import com.cinema.dto.SnackOrderItem;
import com.cinema.entity.*;
import com.cinema.mapper.*;
import com.cinema.mq.OrderMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderSeatMapper orderSeatMapper;

    @Mock
    private OrderSnackMapper orderSnackMapper;

    @Mock
    private ScreeningMapper screeningMapper;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private CinemaMapper cinemaMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SnackService snackService;

    @Mock
    private RedisSeatService redisSeatService;

    @InjectMocks
    private OrderService orderService;

    // ==================== createOrderFromMessage ====================

    @Test
    void createOrderSuccessWithoutSnacks() {
        Screening screening = new Screening();
        screening.setId(1L);
        screening.setCinemaId(1L);
        screening.setPrice(new BigDecimal("50.00"));

        OrderMessage message = new OrderMessage();
        message.setUserId(100L);
        message.setScreeningId(1L);
        message.setSeats(List.of(new SeatPosition(1, 2), new SeatPosition(1, 3)));
        message.setSnacks(null);
        message.setLockToken("token-123");

        when(screeningMapper.selectById(1L)).thenReturn(screening);
        when(orderSeatMapper.findSoldSeats(1L)).thenReturn(List.of());
        when(redisSeatService.seatKey(1, 2)).thenReturn("1-2");
        when(redisSeatService.seatKey(1, 3)).thenReturn("1-3");

        Order order = orderService.createOrderFromMessage(message);

        assertNotNull(order);
        assertEquals("PAID", order.getStatus());
        assertEquals(new BigDecimal("100.00"), order.getTotalPrice());
        assertEquals(BigDecimal.ZERO, order.getSnackTotal());
        assertEquals(100L, order.getUserId());
        verify(orderMapper).insert(any(Order.class));
        verify(orderSeatMapper, times(2)).insert(any(OrderSeat.class));
        verify(redisSeatService).releaseByToken(eq("token-123"), anyList(), eq(1L));
    }

    @Test
    void createOrderSuccessWithSnacks() {
        Screening screening = new Screening();
        screening.setId(1L);
        screening.setCinemaId(1L);
        screening.setPrice(new BigDecimal("50.00"));

        SnackOrderItem snackItem = new SnackOrderItem();
        snackItem.setSnackId(1L);
        snackItem.setQuantity(2);

        Snack snack = new Snack();
        snack.setId(1L);
        snack.setName("爆米花");
        snack.setPrice(new BigDecimal("25.00"));
        snack.setStatus("ON_SALE");

        OrderMessage message = new OrderMessage();
        message.setUserId(100L);
        message.setScreeningId(1L);
        message.setSeats(List.of(new SeatPosition(1, 1)));
        message.setSnacks(List.of(snackItem));
        message.setLockToken("token-456");

        when(screeningMapper.selectById(1L)).thenReturn(screening);
        when(orderSeatMapper.findSoldSeats(1L)).thenReturn(List.of());
        when(redisSeatService.seatKey(1, 1)).thenReturn("1-1");
        when(snackService.getById(1L)).thenReturn(snack);

        Order order = orderService.createOrderFromMessage(message);

        assertNotNull(order);
        assertEquals(new BigDecimal("100.00"), order.getTotalPrice()); // 50 + 25*2
        assertEquals(new BigDecimal("50.00"), order.getSnackTotal());
        verify(orderSnackMapper).insert(any(OrderSnack.class));
    }

    @Test
    void createOrderFailsWhenScreeningNotFound() {
        OrderMessage message = new OrderMessage();
        message.setScreeningId(999L);

        when(screeningMapper.selectById(999L)).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> orderService.createOrderFromMessage(message));
    }

    @Test
    void createOrderFailsWhenSeatAlreadySold() {
        Screening screening = new Screening();
        screening.setId(1L);
        screening.setCinemaId(1L);
        screening.setPrice(new BigDecimal("50.00"));

        OrderMessage message = new OrderMessage();
        message.setUserId(100L);
        message.setScreeningId(1L);
        message.setSeats(List.of(new SeatPosition(2, 4)));
        message.setLockToken("token-789");

        when(screeningMapper.selectById(1L)).thenReturn(screening);
        when(orderSeatMapper.findSoldSeats(1L)).thenReturn(List.of("2-4"));
        when(redisSeatService.seatKey(2, 4)).thenReturn("2-4");

        assertThrows(IllegalStateException.class,
                () -> orderService.createOrderFromMessage(message));
    }

    // ==================== listUserOrders ====================

    @Test
    void listUserOrdersReturnsOrdersWithDetails() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("C123-100");
        order.setUserId(100L);
        order.setScreeningId(1L);
        order.setCinemaId(1L);
        order.setTotalPrice(new BigDecimal("100.00"));
        order.setSnackTotal(BigDecimal.ZERO);
        order.setStatus("PAID");
        order.setCreatedAt(LocalDateTime.now());

        Cinema cinema = new Cinema();
        cinema.setName("万达影城");
        cinema.setAddress("深圳市南山区");

        Screening screening = new Screening();
        screening.setId(1L);
        screening.setMovieId(1L);
        screening.setHallName("1号厅");

        Movie movie = new Movie();
        movie.setTitle("流浪地球");
        movie.setPoster("/poster.jpg");

        when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(order));
        when(cinemaMapper.selectById(1L)).thenReturn(cinema);
        when(screeningMapper.selectById(1L)).thenReturn(screening);
        when(movieMapper.selectById(1L)).thenReturn(movie);
        when(orderSeatMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(orderSnackMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<Map<String, Object>> result = orderService.listUserOrders(100L);
        assertEquals(1, result.size());
        assertEquals("C123-100", result.get(0).get("orderNo"));
        assertEquals("万达影城", result.get(0).get("cinemaName"));
        assertEquals("流浪地球", result.get(0).get("movieTitle"));
    }

    @Test
    void listUserOrdersReturnsEmptyList() {
        when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        List<Map<String, Object>> result = orderService.listUserOrders(999L);
        assertTrue(result.isEmpty());
    }

    // ==================== dashboardStats ====================

    @Test
    void dashboardStatsReturnsCorrectCounts() {
        when(orderMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);
        when(userMapper.selectCount(null)).thenReturn(50L);
        when(movieMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
        when(orderMapper.revenueByDay()).thenReturn(List.of(Map.of("date", "2024-01-01", "revenue", 5000)));
        when(orderMapper.topMovies()).thenReturn(List.of(Map.of("title", "流浪地球", "revenue", 3000)));

        Map<String, Object> stats = orderService.dashboardStats();
        assertEquals(10L, stats.get("totalOrders"));
        assertEquals(50L, stats.get("totalUsers"));
        assertEquals(5L, stats.get("totalMovies"));
        assertNotNull(stats.get("revenueByDay"));
        assertNotNull(stats.get("topMovies"));
    }

    // ==================== listAllOrders ====================

    @Test
    void listAllOrdersReturnsOrdersWithUserInfo() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("C123-100");
        order.setUserId(100L);
        order.setScreeningId(1L);
        order.setCinemaId(1L);
        order.setTotalPrice(new BigDecimal("100.00"));
        order.setSnackTotal(BigDecimal.ZERO);
        order.setStatus("PAID");
        order.setCreatedAt(LocalDateTime.now());

        User user = new User();
        user.setUsername("testuser");

        Cinema cinema = new Cinema();
        cinema.setName("万达影城");

        Screening screening = new Screening();
        screening.setId(1L);
        screening.setMovieId(1L);
        screening.setHallName("IMAX厅");

        Movie movie = new Movie();
        movie.setTitle("流浪地球");

        when(orderMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(order));
        when(userMapper.selectById(100L)).thenReturn(user);
        when(cinemaMapper.selectById(1L)).thenReturn(cinema);
        when(screeningMapper.selectById(1L)).thenReturn(screening);
        when(movieMapper.selectById(1L)).thenReturn(movie);

        List<Map<String, Object>> result = orderService.listAllOrders();
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).get("username"));
        assertEquals("流浪地球", result.get(0).get("movieTitle"));
    }
}
