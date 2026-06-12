package com.cinema.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka 主题配置
 */
@Configuration
public class KafkaConfig {

    public static final String ORDER_EVENTS_TOPIC = "order-events";
    /** 分区数，配合消费者并行消费 */
    private static final int PARTITIONS = 3;
    /** 副本数（单机 Kafka 只能为 1） */
    private static final short REPLICAS = 1;

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(ORDER_EVENTS_TOPIC)
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .build();
    }
}
