package com.cinema.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cinema.dto.SnackOrderItem;
import com.cinema.entity.*;
import com.cinema.mapper.*;
import com.cinema.mq.OrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderSeatMapper orderSeatMapper;
    private final OrderSnackMapper orderSnackMapper;
    private final ScreeningMapper screeningMapper;
    private final MovieMapper movieMapper;
    private final CinemaMapper cinemaMapper;
    private final UserMapper userMapper;
    private final SnackService snackService;
    private final RedisSeatService redisSeatService;

    @Transactional
    public Order createOrderFromMessage(OrderMessage message) {
        Screening screening = screeningMapper.selectById(message.getScreeningId());
        if (screening == null) {
            throw new IllegalStateException("场次不存在");
        }
        List<String> sold = orderSeatMapper.findSoldSeats(message.getScreeningId());
        for (var seat : message.getSeats()) {
            String key = redisSeatService.seatKey(seat.getRow(), seat.getCol());
            if (sold.contains(key)) {
                throw new IllegalStateException("座位冲突: " + key);
            }
        }

        BigDecimal ticketTotal = screening.getPrice().multiply(BigDecimal.valueOf(message.getSeats().size()));
        BigDecimal snackTotal = calcSnackTotal(message.getSnacks());
        Order order = new Order();
        order.setOrderNo("C" + System.currentTimeMillis() + "-" + message.getUserId());
        order.setUserId(message.getUserId());
        order.setScreeningId(message.getScreeningId());
        order.setCinemaId(screening.getCinemaId());
        order.setSnackTotal(snackTotal);
        order.setTotalPrice(ticketTotal.add(snackTotal));
        order.setStatus("PAID");
        order.setCreatedAt(LocalDateTime.now());
        order.setPaidAt(LocalDateTime.now());
        orderMapper.insert(order);

        for (var seat : message.getSeats()) {
            OrderSeat os = new OrderSeat();
            os.setOrderId(order.getId());
            os.setRowNum(seat.getRow());
            os.setColNum(seat.getCol());
            orderSeatMapper.insert(os);
        }

        saveOrderSnacks(order.getId(), message.getSnacks());
        redisSeatService.releaseByToken(message.getLockToken(), message.getSeats(), message.getScreeningId());
        log.info("Order {} created for user {}, snackTotal={}", order.getOrderNo(), message.getUserId(), snackTotal);
        return order;
    }

    private BigDecimal calcSnackTotal(List<SnackOrderItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (SnackOrderItem item : items) {
            Snack snack = snackService.getById(item.getSnackId());
            total = total.add(snack.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
    }

    private void saveOrderSnacks(Long orderId, List<SnackOrderItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (SnackOrderItem item : items) {
            Snack snack = snackService.getById(item.getSnackId());
            OrderSnack os = new OrderSnack();
            os.setOrderId(orderId);
            os.setSnackId(snack.getId());
            os.setSnackName(snack.getName());
            os.setQuantity(item.getQuantity());
            os.setUnitPrice(snack.getPrice());
            orderSnackMapper.insert(os);
        }
    }

    public List<Map<String, Object>> listUserOrders(Long userId) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId).orderByDesc(Order::getCreatedAt));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order order : orders) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", order.getId());
            item.put("orderNo", order.getOrderNo());
            item.put("totalPrice", order.getTotalPrice());
            item.put("snackTotal", order.getSnackTotal());
            item.put("status", order.getStatus());
            item.put("createdAt", order.getCreatedAt());

            if (order.getCinemaId() != null) {
                Cinema cinema = cinemaMapper.selectById(order.getCinemaId());
                if (cinema != null) {
                    item.put("cinemaName", cinema.getName());
                    item.put("cinemaAddress", cinema.getAddress());
                }
            }

            Screening screening = screeningMapper.selectById(order.getScreeningId());
            if (screening != null) {
                item.put("hallName", screening.getHallName());
                item.put("startTime", screening.getStartTime());
                Movie movie = movieMapper.selectById(screening.getMovieId());
                if (movie != null) {
                    item.put("movieTitle", movie.getTitle());
                    item.put("poster", movie.getPoster());
                }
            }

            List<OrderSeat> seats = orderSeatMapper.selectList(new LambdaQueryWrapper<OrderSeat>()
                    .eq(OrderSeat::getOrderId, order.getId()));
            item.put("seats", seats.stream()
                    .map(s -> s.getRowNum() + "排" + s.getColNum() + "座")
                    .toList());

            List<OrderSnack> snacks = orderSnackMapper.selectList(new LambdaQueryWrapper<OrderSnack>()
                    .eq(OrderSnack::getOrderId, order.getId()));
            item.put("snacks", snacks.stream()
                    .map(s -> s.getSnackName() + " x" + s.getQuantity())
                    .toList());
            result.add(item);
        }
        return result;
    }

    public Map<String, Object> dashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalOrders", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, "PAID")));
        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("totalMovies", movieMapper.selectCount(new LambdaQueryWrapper<Movie>().eq(Movie::getStatus, "SHOWING")));
        stats.put("revenueByDay", orderMapper.revenueByDay());
        stats.put("topMovies", orderMapper.topMovies());
        return stats;
    }

    public List<Map<String, Object>> listAllOrders() {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreatedAt).last("LIMIT 50"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order order : orders) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", order.getId());
            item.put("orderNo", order.getOrderNo());
            item.put("totalPrice", order.getTotalPrice());
            item.put("snackTotal", order.getSnackTotal());
            item.put("status", order.getStatus());
            item.put("createdAt", order.getCreatedAt());
            User user = userMapper.selectById(order.getUserId());
            if (user != null) item.put("username", user.getUsername());
            if (order.getCinemaId() != null) {
                Cinema cinema = cinemaMapper.selectById(order.getCinemaId());
                if (cinema != null) item.put("cinemaName", cinema.getName());
            }
            Screening screening = screeningMapper.selectById(order.getScreeningId());
            if (screening != null) {
                item.put("hallName", screening.getHallName());
                Movie movie = movieMapper.selectById(screening.getMovieId());
                if (movie != null) item.put("movieTitle", movie.getTitle());
            }
            result.add(item);
        }
        return result;
    }
}
