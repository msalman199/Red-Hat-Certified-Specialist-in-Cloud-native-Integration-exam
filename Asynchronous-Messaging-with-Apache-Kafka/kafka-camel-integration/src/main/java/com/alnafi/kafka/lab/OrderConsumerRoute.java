package com.alnafi.kafka.lab;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

public class OrderConsumerRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        
        // Route to consume all orders and log them
        from("kafka:order-events?brokers=localhost:9092&groupId=order-logger&keyDeserializer=org.apache.kafka.common.serialization.StringDeserializer&valueDeserializer=org.apache.kafka.common.serialization.StringDeserializer")
            .routeId("order-consumer-route")
            .log("Received order from Kafka: ${body}")
            .unmarshal().json(JsonLibrary.Jackson, Order.class)
            .process(exchange -> {
                Order order = exchange.getIn().getBody(Order.class);
                System.out.println("=== ORDER PROCESSED ===");
                System.out.println("Order ID: " + order.getOrderId());
                System.out.println("Customer: " + order.getCustomerId());
                System.out.println("Product: " + order.getProductName());
                System.out.println("Quantity: " + order.getQuantity());
                System.out.println("Price: $" + String.format("%.2f", order.getPrice()));
                System.out.println("Timestamp: " + order.getTimestamp());
                System.out.println("========================");
            });
            
        // Route to consume notifications
        from("kafka:notification-events?brokers=localhost:9092&groupId=notification-processor&keyDeserializer=org.apache.kafka.common.serialization.StringDeserializer&valueDeserializer=org.apache.kafka.common.serialization.StringDeserializer")
            .routeId("notification-consumer-route")
            .log("Received notification: ${body}")
            .process(exchange -> {
                String notification = exchange.getIn().getBody(String.class);
                String alertType = exchange.getIn().getHeader("alertType", String.class);
                String orderId = exchange.getIn().getHeader("orderId", String.class);
                
                System.out.println("*** NOTIFICATION ALERT ***");
                System.out.println("Type: " + alertType);
                System.out.println("Order ID: " + orderId);
                System.out.println("Message: " + notification);
                System.out.println("***************************");
            });
            
        // Route to process orders by customer (example of partitioned consumption)
        from("kafka:order-events?brokers=localhost:9092&groupId=customer-processor&keyDeserializer=org.apache.kafka.common.serialization.StringDeserializer&valueDeserializer=org.apache.kafka.common.serialization.StringDeserializer")
            .routeId("customer-order-processor")
            .unmarshal().json(JsonLibrary.Jackson, Order.class)
            .choice()
                .when(simple("${body.customerId} == 'CUST001'"))
                    .log("VIP Customer order received: ${body}")
                    .process(exchange -> {
                        Order order = exchange.getIn().getBody(Order.class);
                        System.out.println("🌟 VIP CUSTOMER ORDER 🌟");
                        System.out.println("Applying 10% discount for " + order.getCustomerId());
                        order.setPrice(order.getPrice() * 0.9);
                        System.out.println("New price: $" + String.format("%.2f", order.getPrice()));
                    })
                .otherwise()
                    .log("Regular customer order: ${body.customerId}");
    }
}
