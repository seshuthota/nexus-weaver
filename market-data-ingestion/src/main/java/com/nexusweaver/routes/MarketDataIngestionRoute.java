package com.nexusweaver.routes;

import com.nexusweaver.model.MarketDataEvent;
import com.nexusweaver.processors.MarketDataProcessor;
import com.nexusweaver.processors.MessageValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Market Data Ingestion Route
 * 
 * Handles multiple data sources:
 * - Timer-based simulated market data for testing
 * - HTTP REST endpoints
 * - Direct processing pipeline
 */
@Slf4j
@Component
public class MarketDataIngestionRoute extends RouteBuilder {

    @Autowired
    private MarketDataProcessor marketDataProcessor;

    @Autowired
    private MessageValidator messageValidator;

    @Override
    public void configure() throws Exception {
        
        // Error handling
        onException(Exception.class)
            .handled(true)
            .log("Error processing market data: ${exception.message}")
            .to("micrometer:counter:market.data.errors");

        // Simulated market data generator for testing (every 5 seconds in dev)
        from("timer:market-data-generator?period=5000")
            .routeId("market-data-generator")
            .log("Generating sample market data")
            .process(exchange -> {
                MarketDataEvent event = MarketDataEvent.createSample();
                exchange.getIn().setBody(event);
                exchange.getIn().setHeader("source", "SIMULATOR");
                exchange.getIn().setHeader("symbol", event.getSymbol());
            })
            .marshal().json(JsonLibrary.Jackson)
            .to("direct:process-market-data");

        // HTTP REST endpoint for market data
        from("servlet:market-data?httpMethodRestrict=POST")
            .routeId("http-market-data")
            .log("Received HTTP market data: ${body}")
            .setHeader("source", constant("HTTP"))
            .unmarshal().json(JsonLibrary.Jackson, MarketDataEvent.class)
            .to("direct:process-market-data");

        // Main processing pipeline
        from("direct:process-market-data")
            .routeId("market-data-processor")
            .log("Processing market data from ${header.source}")
            
            // Validate message
            .process(messageValidator)
            
            // Add processing metadata
            .process(exchange -> {
                MarketDataEvent event = exchange.getIn().getBody(MarketDataEvent.class);
                exchange.getIn().setHeader("symbol", event.getSymbol());
                exchange.getIn().setHeader("exchange", event.getExchange());
                exchange.getIn().setHeader("messageType", event.getMessageType());
                exchange.getIn().setHeader("timestamp", event.getTimestamp());
            })
            
            // Process market data
            .process(marketDataProcessor)
            
            // Add metrics
            .to("micrometer:counter:market.data.processed.total?tags=source=${header.source},symbol=${header.symbol}")
            .to("micrometer:timer:market.data.processing.latency")
            
            // Route based on message type
            .choice()
                .when(header("messageType").isEqualTo("MARKET_DATA"))
                    .log("Routing market data for ${header.symbol}")
                    .to("direct:market-data-output")
                .when(header("messageType").isEqualTo("TRADE"))
                    .log("Routing trade data for ${header.symbol}")
                    .to("direct:trade-output")
                .otherwise()
                    .log("Unknown message type: ${header.messageType}")
                    .to("direct:unknown-output")
            .end();

        // Output routes (for now just logging, will connect to RabbitMQ later)
        from("direct:market-data-output")
            .routeId("market-data-output")
            .log("Market data processed: Symbol=${header.symbol}, Price=${body.price}, Volume=${body.volume}")
            .marshal().json(JsonLibrary.Jackson)
            .to("log:market-data-output?level=INFO&showBody=true");

        from("direct:trade-output")
            .routeId("trade-output")
            .log("Trade data processed: ${body}")
            .marshal().json(JsonLibrary.Jackson)
            .to("log:trade-output?level=INFO&showBody=true");

        from("direct:unknown-output")
            .routeId("unknown-output")
            .log("Unknown data type processed: ${body}")
            .to("log:unknown-output?level=WARN&showBody=true");
    }
} 