package com.cinema.kafka;

import com.cinema.dto.SeatPosition;
import com.cinema.dto.SnackOrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Kafka 订单事件 — 购票成功后发出，用于异步数据分析 / 事件溯源
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent implements Serializable {
    private String  eventId;        // 事件唯一 ID
    private String  orderNo;        // 订单号
    private Long    userId;         // 用户 ID
    private Long    screeningId;    // 场次 ID
    private Long    cinemaId;       // 影院 ID
    private List<SeatPosition> seats;       // 座位列表
    private List<SnackOrderItem> snacks;    // 零食列表
    private BigDecimal totalAmount; // 总金额
    private String  status;         // PAID
    private LocalDateTime eventTime;        // 事件发生时间
}
