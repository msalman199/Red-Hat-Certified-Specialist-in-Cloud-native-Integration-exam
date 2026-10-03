package com.alnafi.kafka.lab;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import java.util.Random;

public class OrderProducerRoute extends RouteBuilder {
    
    private final Random random = new Random();
    private final String[] products = {"Laptop", "Smartphone", "Tablet", "Headphones", "Monitor"};
    private final String[] customers = {"CUST001", "CUST002", "CUST003", "CUST004", "CUST005"};
    
    @Override
    public void configure() throws Exception {
        
        // Route to generate and send orders to Kafka
        from("timer:orderGenerator?period=5000")
            .routeId("order-producer-route")
            .log("Generating new order...")
            .process(exchange -> {
                // Create a random order
                String orderId = "ORD" + System.currentTimeMillis();
                String customerId = customers[random.nextInt(customers.length)];
                String productName = products[random.nextInt(products.length)];
                int quantity = random.nextInt(5) + 1;
                double price = (random.nextDouble() * 1000) + 100;
                
                Order order = new Order(orderId, customerId, productName, quantity, price);
                exchange.getIn().setBody(order);
                exchange.getIn().setHeader("orderId", orderId);
                exchange.getIn().setHeader("customerId", customerId);
            })
            .marshal().json(JsonLibrary.Jackson)
            .log("Sending order to Kafka: ${body}")
            .to("kafka:order-events?brokers=localhost:9092&keySerializer=org.apache.kafka.common.serialization.StringSerializer&valueSerializer=org.apache.kafka.common.serialization.StringSerializer")
            .log("Order sent successfully");
            
        // Route to handle high-value orders (> $500)
        from("kafka:order-events?brokers=localhost:9092&groupId=high-value-processor&keyDeserializer=org.apache.kafka.common.serialization.StringDeserializer&valueDeserializer=org.apache.kafka.common.serialization.StringDeserializer")
            .routeId("high-value-order-processor")
            .unmarshal().json(JsonLibrary.Jackson, Order.class)
            .filter(simple("${body.price} > 500"))
            .log("Processing high-value order: ${body}")
            .process(exchange -> {
                Order order = exchange.getIn().getBody(Order.class);
                String notification = String.format(
                    "HIGH VALUE ALERT: Order %s for customer %s - Amount: $%.2f",
                    order.getOrderId(), order.getCustomerId(), order.getPrice()
                );
                exchange.getIn().setBody(notification);
                exchange.getIn().setHeader("alertType", "HIGH_VALUE");
                exchange.getIn().setHeader("orderId", order.getOrderId());
            })
            .to("kafka:notification-events?brokers=localhost:9092&keySerializer=org.apache.kafka.common.serialization.StringSerializer&valueSerializer=org.apache.kafka.common.serialization.StringSerializer")
            .log("High-value notification sent: ${body}");
    }
}
