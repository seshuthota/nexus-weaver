package com.nexusweaver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Nexus Weaver - Market Data Ingestion Service
 * 
 * High-frequency trading platform processing 50M+ market data ticks/second
 * with sub-millisecond latency requirements using Apache Camel integration.
 */
@SpringBootApplication
@EnableScheduling
public class MarketDataIngestionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketDataIngestionApplication.class, args);
    }
} 