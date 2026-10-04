package com.alnafi.camel.eip.splitter;

import org.apache.camel.builder.RouteBuilder;
import java.util.Arrays;
import java.util.List;

public class SplitterRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Generates a sample order every 5 seconds, up to 2 times
        from("timer:orderGenerator?period=5000&repeatCount=2")
            .routeId("order-generator")
            .log("Generating sample order...")
            .process(exchange -> {
                List<OrderItem> items = Arrays.asList(
                    new OrderItem("P001", "Laptop", 2, 999.99),
                    new OrderItem("P002", "Mouse", 3, 29.99),
                    new OrderItem("P003", "Keyboard", 2, 79.99),
                    new OrderItem("P004", "Monitor", 1, 299.99)
                );

                Order order = new Order("ORD-" + System.currentTimeMillis(), "CUST-001", items);
                exchange.getIn().setBody(order);
                exchange.getIn().setHeader("originalOrderId", order.getOrderId());
            })
            .log("Created order: ${body}")
            .to("direct:splitOrder");

        // Splitter route - splits the order into individual OrderItem messages
        from("direct:splitOrder")
            .routeId("order-splitter")
            .log("Splitting order into individual items...")
            .split(simple("${body.items}"))
                .streaming()
                .process(exchange -> {
                    OrderItem item = exchange.getIn().getBody(OrderItem.class);
                    String originalOrderId = exchange.getIn().getHeader("originalOrderId", String.class);

                    exchange.getIn().setHeader("correlationId", originalOrderId);
                    exchange.getIn().setHeader("itemId", item.getProductId());

                    log.info("Split item - OrderID: {}, ProductID: {}, Product: {}",
                            originalOrderId, item.getProductId(), item.getProductName());
                })
                .to("direct:processItem")
            .end()
            .log("Order splitting completed");

        // Individual item processing route
        from("direct:processItem")
            .routeId("item-processor")
            .log("Processing individual item: ${header.itemId}")
            .process(exchange -> {
                OrderItem item = exchange.getIn().getBody(OrderItem.class);
                double totalPrice = item.getQuantity() * item.getPrice();
                exchange.getIn().setHeader("itemTotal", totalPrice);
                log.info("Processed item {} - Total: {}", item.getProductId(), totalPrice);
            })
            .to("direct:aggregateItems");
    }
}
