package com.nexusweaver.processors;

import com.nexusweaver.model.MarketDataEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Random;

/**
 * Market Data Processor
 * Enriches and transforms market data events for further processing
 */
@Slf4j
@Component
public class MarketDataProcessor implements Processor {

    private final Random random = new Random();

    @Override
    public void process(Exchange exchange) throws Exception {
        MarketDataEvent event = exchange.getIn().getBody(MarketDataEvent.class);
        
        if (event == null) {
            throw new IllegalArgumentException("Market data event cannot be null");
        }

        // Enrich with calculated fields
        enrichMarketData(event);
        
        // Add processing metadata
        exchange.getIn().setHeader("processed", true);
        exchange.getIn().setHeader("processingTime", Instant.now());
        exchange.getIn().setHeader("enriched", true);
        
        // Update the body with enriched event
        exchange.getIn().setBody(event);
        
        log.debug("Market data processed and enriched: symbol={}, spread={}", 
                 event.getSymbol(), calculateSpread(event));
    }

    private void enrichMarketData(MarketDataEvent event) {
        // Generate bid/ask if not present (for simulation)
        if (event.getBid() == null || event.getAsk() == null) {
            generateBidAsk(event);
        }

        // Calculate derived fields
        addDerivedFields(event);
        
        // Add market metadata
        addMarketMetadata(event);
    }

    private void generateBidAsk(MarketDataEvent event) {
        if (event.getPrice() != null) {
            // Generate realistic bid/ask spread (0.01-0.05% of price)
            double spreadPercent = 0.0001 + (random.nextDouble() * 0.0004); // 0.01% to 0.05%
            BigDecimal spread = event.getPrice().multiply(BigDecimal.valueOf(spreadPercent));
            
            if (event.getBid() == null) {
                event.setBid(event.getPrice().subtract(spread.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP)));
            }
            
            if (event.getAsk() == null) {
                event.setAsk(event.getPrice().add(spread.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP)));
            }
        }
    }

    private void addDerivedFields(MarketDataEvent event) {
        // Set message type if not present
        if (event.getMessageType() == null) {
            event.setMessageType("MARKET_DATA");
        }
        
        // Generate sequence number if not present
        if (event.getSequenceNumber() == null) {
            event.setSequenceNumber(System.currentTimeMillis());
        }
        
        // Set default exchange if not present
        if (event.getExchange() == null) {
            event.setExchange(inferExchange(event.getSymbol()));
        }
    }

    private void addMarketMetadata(MarketDataEvent event) {
        // Add processing timestamp
        if (event.getTimestamp() == null) {
            event.setTimestamp(Instant.now());
        }
        
        // Set source if not present
        if (event.getSource() == null) {
            event.setSource("PROCESSED");
        }
    }

    private String inferExchange(String symbol) {
        // Simple heuristic for exchange inference
        if (symbol != null) {
            // Common tech stocks typically trade on NASDAQ
            if (symbol.matches("^(AAPL|GOOGL|MSFT|AMZN|TSLA|META|NFLX)$")) {
                return "NASDAQ";
            }
            // Common financial stocks typically trade on NYSE
            if (symbol.matches("^(JPM|BAC|WFC|C|GS|MS)$")) {
                return "NYSE";
            }
        }
        return "UNKNOWN";
    }

    private BigDecimal calculateSpread(MarketDataEvent event) {
        if (event.getBid() != null && event.getAsk() != null) {
            return event.getAsk().subtract(event.getBid());
        }
        return BigDecimal.ZERO;
    }
} 