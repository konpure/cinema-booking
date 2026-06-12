package com.cinema.kafka;

import com.cinema.dto.SeatPosition;
import com.cinema.dto.SnackOrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Kafka 订单事件发布器
 * 在购票成功后发布 OrderEvent 到 order-events topic
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    /**
     * 发布订单事件
     * @param orderNo     订单号
     * @param userId      用户 ID
     * @param screeningId 场次 ID
     * @param cinemaId    影院 ID
     * @param seats       座位列表
     * @param snacks      零食列表
     * @param totalAmount 总金额
     */
    public void publishOrderEvent(
            String orderNo,
            Long userId,
            Long screeningId,
            Long cinemaId,
            List<SeatPosition> seats,
            List<SnackOrderItem> snacks,
            BigDecimal totalAmount) {

        OrderEvent event = new OrderEvent(
                UUID.randomUUID().toString(),
                orderNo,
                userId,
                screeningId,
                cinemaId,
                seats,
                snacks,
                totalAmount,
                "PAID",
                LocalDateTime.now()
        );

        kafkaTemplate.send("order-events", orderNo, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Kafka event published: orderNo={}, offset={}",
                                orderNo, result.getRecordMetadata().offset());
                    } else {
                        log.error("Kafka event publish failed: orderNo={}", orderNo, ex);
                    }
                });
    }
}
