package com.cinema.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "cinema.kafka.enabled", havingValue = "true")
public class OrderEventConsumer {

    @KafkaListener(
            topics = "order-events",
            groupId = "cinema-order-group",
            containerFactory = "kafkaListenerContainerFactory",
            autoStartup = "${cinema.kafka.enabled:false}"
    )
    public void consume(OrderEvent event) {
        log.info("===== Kafka Order Event Received =====");
        log.info("EventId:    {}", event.getEventId());
        log.info("OrderNo:    {}", event.getOrderNo());
        log.info("UserId:     {}", event.getUserId());
        log.info("ScreeningId:{}", event.getScreeningId());
        log.info("Seats:      {}", event.getSeats());
        log.info("TotalAmount:{}", event.getTotalAmount());
        log.info("EventTime:  {}", event.getEventTime());
        log.info("======================================");
    }
}