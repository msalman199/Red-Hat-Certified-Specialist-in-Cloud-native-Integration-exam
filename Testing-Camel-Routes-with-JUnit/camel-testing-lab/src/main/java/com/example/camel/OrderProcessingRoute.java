package com.example.camel;

import org.apache.camel.builder.RouteBuilder;

public class OrderProcessingRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Route: order priority routing
        from("direct:processOrder")
            .routeId("order-processing-route")
            .log("Processing order: ${body}")
            .choice()
                .when(xpath("/order/priority[text()='HIGH']"))
                    .log("High priority order detected")
                    .to("direct:highPriorityQueue")
                .when(xpath("/order/priority[text()='MEDIUM']"))
                    .log("Medium priority order detected")
                    .to("direct:mediumPriorityQueue")
                .otherwise()
                    .log("Low priority order detected")
                    .to("direct:lowPriorityQueue")
            .end();

        // Route: order validation
        from("direct:validateOrder")
            .routeId("order-validation-route")
            .log("Validating order: ${body}")
            .choice()
                .when(xpath("/order/customerId[text()='']"))
                    .throwException(new IllegalArgumentException("Customer ID is required"))
                .when(xpath("/order/amount[number(.) <= 0]"))
                    .throwException(new IllegalArgumentException("Order amount must be positive"))
                .otherwise()
                    .log("Order validation successful")
                    .setHeader("ValidationStatus", constant("VALID"))
            .end();

        // Route: order transformation - extract customer id as the new body
        from("direct:transformOrder")
            .routeId("order-transformation-route")
            .log("Transforming order format")
            .setHeader("ProcessedTimestamp", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
            .transform().xpath("/order/customerId/text()", String.class)
            .setHeader("CustomerId", body())
            .to("direct:orderTransformed");
    }
}
