package com.nexusweaver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Market Data Consumer Service
 * 
 * This microservice consumes market data from RabbitMQ streams
 * and performs distributed processing in cluster mode.
 */
@SpringBootApplication
public class MarketDataConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketDataConsumerApplication.class, args);
    }
} 