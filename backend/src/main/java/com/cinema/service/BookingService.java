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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cinema.config.RabbitMQConfig.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final ScreeningMapper screeningMapper;
    private final OrderSeatMapper orderSeatMapper;
    private final RedisSeatService redisSeatService;
    private final RabbitTemplate rabbitTemplate;
    private final OrderService orderService;

    public SeatMapResponse getSeatMap(Long screeningId, Long currentUserId) {
        Screening screening = screeningMapper.selectById(screeningId);
        if (screening == null) {
            throw new IllegalArgumentException("场次不存在");
        }
        List<String> sold = orderSeatMapper.findSoldSeats(screeningId);
        var locks = redisSeatService.getLockedSeatsByUser(screeningId, currentUserId);
        return new SeatMapResponse(
                screeningId,
                screening.getSeatRows(),
                screening.getSeatCols(),
                sold,
                locks.get("locked"),
                locks.get("myLocked")
        );
    }

    public Map<String, String> lockSeats(Long userId, LockSeatsRequest request) {
        Screening screening = requireOpenScreening(request.getScreeningId());
        validateSeats(screening, request.getSeats());
        List<String> sold = orderSeatMapper.findSoldSeats(request.getScreeningId());
        String token = redisSeatService.lockSeats(request.getScreeningId(), userId, request.getSeats(), sold);
        Map<String, String> result = new HashMap<>();
        result.put("lockToken", token);
        result.put("message", "座位已锁定，请在 10 分钟内完成下单");
        return result;
    }

    public Map<String, String> submitOrder(Long userId, LockSeatsRequest request, String lockToken,
                                           List<SnackOrderItem> snacks) {
        Screening screening = requireOpenScreening(request.getScreeningId());
        validateSeats(screening, request.getSeats());
        if (!redisSeatService.validateToken(lockToken, userId, request.getScreeningId())) {
            throw new IllegalStateException("座位锁定已过期，请重新选座");
        }
        OrderMessage message = new OrderMessage(
                userId, request.getScreeningId(), screening.getCinemaId(),
                request.getSeats(), snacks, lockToken);
        Order order;
        try {
            order = orderService.createOrderFromMessage(message);
        } catch (RuntimeException e) {
            redisSeatService.releaseByToken(lockToken, request.getSeats(), request.getScreeningId());
            throw e;
        }
        publishOrderNotification(order);
        Map<String, String> result = new HashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("message", "购票成功");
        return result;
    }

    public void releaseLock(Long userId, String lockToken, Long screeningId, List<SeatPosition> seats) {
        if (redisSeatService.validateToken(lockToken, userId, screeningId)) {
            redisSeatService.releaseByToken(lockToken, seats, screeningId);
        }
    }

    private Screening requireOpenScreening(Long screeningId) {
        Screening screening = screeningMapper.selectById(screeningId);
        if (screening == null) {
            throw new IllegalArgumentException("场次不存在");
        }
        if (!"OPEN".equals(screening.getStatus())) {
            throw new IllegalStateException("该场次已关闭");
        }
        return screening;
    }

    private void validateSeats(Screening screening, List<SeatPosition> seats) {
        for (SeatPosition seat : seats) {
            if (seat.getRow() == null || seat.getCol() == null) {
                throw new IllegalArgumentException("座位坐标无效");
            }
            if (seat.getRow() < 1 || seat.getRow() > screening.getSeatRows()
                    || seat.getCol() < 1 || seat.getCol() > screening.getSeatCols()) {
                throw new IllegalArgumentException("座位 " + seat.getRow() + "-" + seat.getCol() + " 超出范围");
            }
        }
    }

    private void publishOrderNotification(Order order) {
        try {
            OrderNotification notification = new OrderNotification(
                    order.getOrderNo(), order.getUserId(), order.getScreeningId());
            rabbitTemplate.convertAndSend(ORDER_EXCHANGE, ORDER_ROUTING_KEY, notification);
            log.info("Order notification sent to RabbitMQ: {}", order.getOrderNo());
        } catch (Exception e) {
            log.warn("RabbitMQ notification failed for order {} (order already created)", order.getOrderNo(), e);
        }
    }
}
