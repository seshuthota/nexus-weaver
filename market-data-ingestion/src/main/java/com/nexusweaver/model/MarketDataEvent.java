package com.nexusweaver.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Market Data Event representing a financial instrument price tick
 * Used for high-frequency trading data processing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketDataEvent {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("volume")
    private Long volume;

    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
    private Instant timestamp;

    @JsonProperty("exchange")
    private String exchange;

    @JsonProperty("source")
    private String source;

    @JsonProperty("bid")
    private BigDecimal bid;

    @JsonProperty("ask")
    private BigDecimal ask;

    @JsonProperty("high")
    private BigDecimal high;

    @JsonProperty("low")
    private BigDecimal low;

    @JsonProperty("open")
    private BigDecimal open;

    @JsonProperty("messageType")
    private String messageType;

    @JsonProperty("sequenceNumber")
    private Long sequenceNumber;

    // Metadata for processing
    @JsonProperty("portfolioId")
    private String portfolioId;

    @JsonProperty("traderId")
    private String traderId;

    public static MarketDataEvent createSample() {
        return MarketDataEvent.builder()
                .symbol("AAPL")
                .price(new BigDecimal("150.25"))
                .volume(1000L)
                .timestamp(Instant.now())
                .exchange("NASDAQ")
                .source("TEST")
                .bid(new BigDecimal("150.20"))
                .ask(new BigDecimal("150.30"))
                .messageType("MARKET_DATA")
                .sequenceNumber(1L)
                .build();
    }
} 