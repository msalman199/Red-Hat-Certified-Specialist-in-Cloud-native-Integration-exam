package com.example.camel;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Camel Route Builder for ActiveMQ integration.
 */
public class CamelRouteBuilder extends RouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CamelRouteBuilder.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final Random random = new Random();

    @Override
    public void configure() throws Exception {

        logger.info("Configuring Camel routes for ActiveMQ integration");

        // Global error handling: unhandled exceptions go to a dead letter queue
        errorHandler(deadLetterChannel("jms:queue:dead.letter.queue")
                .maximumRedeliveries(2)
                .redeliveryDelay(1000));

        // Route 1: Simple message transformation
        from("jms:queue:input.queue")
                .routeId("simple-transformation-route")
                .log("Received message from input.queue: ${body}")
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    String timestamp = LocalDateTime.now().format(FORMATTER);
                    String processed = String.format("Processed at %s: %s", timestamp, body.toUpperCase());
                    exchange.getIn().setBody(processed);
                    exchange.getIn().setHeader("ProcessedAt", timestamp);
                })
                .log("Sending processed message to output.queue: ${body}")
                .to("jms:queue:output.queue");

        // Route 2: Order processing with content-based routing
        from("jms:queue:order.queue")
                .routeId("order-processing-route")
                .log("Received order from order.queue: ${body}")
                .process(exchange -> {
                    String orderMessage = exchange.getIn().getBody(String.class);
                    String orderId = "ORD-" + (1000 + random.nextInt(9000));
                    String status = random.nextBoolean() ? "APPROVED" : "PENDING";
                    String processedOrder = String.format(
                            "Order ID: %s | Status: %s | Original: %s", orderId, status, orderMessage);
                    exchange.getIn().setBody(processedOrder);
                    exchange.getIn().setHeader("OrderStatus", status);
                })
                .choice()
                    .when(header("OrderStatus").isEqualTo("APPROVED"))
                        .log("Order approved, sending to fulfillment: ${body}")
                        .to("jms:queue:fulfillment.queue")
                    .otherwise()
                        .log("Order pending, sending to review: ${body}")
                        .to("jms:queue:review.queue")
                .end();

        // Route 3: Risky processing route demonstrating dead letter handling
        from("jms:queue:risky.queue")
                .routeId("risky-processing-route")
                .log("Processing risky message: ${body}")
                .process(exchange -> {
                    if (random.nextDouble() < 0.3) {
                        throw new RuntimeException("Simulated processing error");
                    }
                    String body = exchange.getIn().getBody(String.class);
                    exchange.getIn().setBody("Successfully processed: " + body);
                })
                .log("Risky message processed successfully: ${body}")
                .to("jms:queue:success.queue");

        logger.info("Camel routes configured successfully");
    }
}
