package com.cinema.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.cinema.config.RabbitMQConfig.ORDER_QUEUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    @RabbitListener(queues = ORDER_QUEUE)
    public void handleOrderNotification(OrderNotification notification) {
        log.info("Async order notification processed: orderNo={} user={} screening={}",
                notification.getOrderNo(), notification.getUserId(), notification.getScreeningId());
    }
}
