package com.nathan.memex;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public Queue ingestionQueue() {
        return new Queue("memex-ingestion-queue", true);
    }
}