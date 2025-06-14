package com.nexusweaver.routes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

/**
 * Market Data Consumer Route
 * 
 * Distributed consumer that reads from RabbitMQ streams
 * and performs scalable processing in cluster mode.
 */
@Slf4j
@Component
public class MarketDataConsumerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        
        // Error handling for resilient processing
        onException(Exception.class)
            .handled(true)
            .log("Error consuming market data: ${exception.message}")
            .to("micrometer:counter:market.data.consumer.errors");

        // CONSUMER ROUTE - Reads from RabbitMQ stream with consumer group for load balancing
        from("rabbitmq:market-prices-stream?hostname={{rabbitmq.hostname}}&port=5672&username={{rabbitmq.username}}&password={{rabbitmq.password}}&queue=market-prices-consumer-group&routingKey=market.data&concurrentConsumers=5&args=#{'x-stream-offset':'last','x-consumer-group':'nexus-consumer-group'}")
            .routeId("market-data-stream-consumer")
            .log("CONSUMER [${env:HOSTNAME}]: Received market data message from stream")
            
            // Unmarshal JSON
            .unmarshal().json(JsonLibrary.Jackson)
            
            // Add consumer metadata
            .process(exchange -> {
                exchange.getIn().setHeader("consumerInstance", System.getenv("POD_NAME"));
                exchange.getIn().setHeader("processedAt", System.currentTimeMillis());
            })
            
            // Add metrics for distributed processing
            .to("micrometer:counter:market.data.consumed.total")
            .to("micrometer:timer:market.data.consumer.latency")
            
            // Route to different processing based on content
            .choice()
                .when(jsonpath("$[?(@.symbol)]"))
                    .log("CONSUMER: Processing market data for symbol: ${jsonpath($.symbol)}")
                    .to("direct:process-market-data")
                .otherwise()
                    .log("CONSUMER: Unknown message format")
                    .to("direct:handle-unknown")
            .end();

        // Trade execution consumer (second stream with consumer group)
        from("rabbitmq:trade-executions-stream?hostname={{rabbitmq.hostname}}&port=5672&username={{rabbitmq.username}}&password={{rabbitmq.password}}&queue=trade-executions-consumer-group&routingKey=trade.execution&concurrentConsumers=3&args=#{'x-stream-offset':'last','x-consumer-group':'nexus-trade-group'}")
            .routeId("trade-executions-stream-consumer")
            .log("CONSUMER [${env:HOSTNAME}]: Received trade execution message from stream")
            .unmarshal().json(JsonLibrary.Jackson)
            .to("micrometer:counter:trade.executions.consumed.total")
            .to("direct:process-trade-execution");

        // Market data processing sub-route
        from("direct:process-market-data")
            .routeId("process-market-data")
            .log("DISTRIBUTED PROCESSING: Market data for ${jsonpath($.symbol)} on pod ${header.consumerInstance}")
            .choice()
                .when(jsonpath("$[?(@.price > 100)]"))
                    .log("HIGH-VALUE STOCK: ${jsonpath($.symbol)} = $${jsonpath($.price)}")
                    .to("direct:high-value-processing")
                .otherwise()
                    .log("REGULAR STOCK: ${jsonpath($.symbol)} = $${jsonpath($.price)}")
                    .to("direct:regular-processing")
            .end();

        // High-value stock processing (could be complex algorithms)
        from("direct:high-value-processing")
            .routeId("high-value-processing")
            .log("CLUSTER PROCESSING: Applying sophisticated algorithms for high-value stock")
            .delay(100) // Simulate complex processing
            .to("micrometer:counter:high.value.stocks.processed");

        // Regular processing
        from("direct:regular-processing")
            .routeId("regular-processing")
            .log("CLUSTER PROCESSING: Standard processing for regular stock")
            .delay(50) // Simulate processing
            .to("micrometer:counter:regular.stocks.processed");

        // Trade execution processing
        from("direct:process-trade-execution")
            .routeId("process-trade-execution")
            .log("TRADE PROCESSING: Processing trade execution on pod ${header.consumerInstance}")
            .to("micrometer:counter:trades.processed");

        // Unknown message handler
        from("direct:handle-unknown")
            .routeId("handle-unknown")
            .log("CONSUMER: Handling unknown message format")
            .to("micrometer:counter:unknown.messages.handled");
    }
} 