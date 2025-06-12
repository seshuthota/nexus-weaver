package com.nexusweaver.processors;

import com.nexusweaver.model.MarketDataEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Message Validator Processor
 * Validates incoming market data events for financial compliance
 */
@Slf4j
@Component
public class MessageValidator implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        MarketDataEvent event = exchange.getIn().getBody(MarketDataEvent.class);
        
        if (event == null) {
            throw new IllegalArgumentException("Market data event cannot be null");
        }

        // Basic validation
        validateBasicFields(event);
        
        // Additional business logic validation
        validateBusinessRules(event);

        // Set timestamp if not present
        if (event.getTimestamp() == null) {
            event.setTimestamp(Instant.now());
        }

        // Add validation metadata
        exchange.getIn().setHeader("validated", true);
        exchange.getIn().setHeader("validationTime", Instant.now());
        
        log.debug("Market data event validated successfully: symbol={}, price={}", 
                 event.getSymbol(), event.getPrice());
    }

    private void validateBasicFields(MarketDataEvent event) {
        if (event.getSymbol() == null || event.getSymbol().trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        
        if (event.getPrice() == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        
        if (event.getPrice().doubleValue() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        
        if (event.getVolume() != null && event.getVolume() <= 0) {
            throw new IllegalArgumentException("Volume must be positive");
        }
    }

    private void validateBusinessRules(MarketDataEvent event) {
        // Financial business rules validation
        
        // Price sanity check (price should be reasonable)
        if (event.getPrice().doubleValue() > 1000000) {
            throw new IllegalArgumentException("Price out of reasonable range: " + event.getPrice());
        }

        // Volume sanity check
        if (event.getVolume() != null && event.getVolume() > 10000000L) {
            log.warn("Unusually high volume detected: {} for symbol {}", event.getVolume(), event.getSymbol());
        }

        // Symbol format validation (basic)
        if (event.getSymbol().length() > 10 || !event.getSymbol().matches("^[A-Z0-9]+$")) {
            throw new IllegalArgumentException("Invalid symbol format: " + event.getSymbol());
        }

        // Bid/Ask spread validation
        if (event.getBid() != null && event.getAsk() != null) {
            if (event.getBid().compareTo(event.getAsk()) >= 0) {
                throw new IllegalArgumentException("Bid price must be less than ask price");
            }
        }
    }
} 