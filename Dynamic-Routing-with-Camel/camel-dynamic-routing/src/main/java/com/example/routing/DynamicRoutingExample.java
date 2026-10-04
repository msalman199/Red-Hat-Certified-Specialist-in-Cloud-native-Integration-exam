// File: src/main/java/com/example/routing/DynamicRoutingExample.java
package com.example.routing;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

public class DynamicRoutingExample extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Route 1: Dynamic routing based on order priority
        from("jetty:http://0.0.0.0:8080/orders")
            .log("Received order: ${body}")
            .choice()
                .when(jsonpath("$.priority[?(@ == 'HIGH')]"))
                    .log("Processing HIGH priority order")
                    .to("direct:highPriorityProcessor")
                .when(jsonpath("$.priority[?(@ == 'MEDIUM')]"))
                    .log("Processing MEDIUM priority order")
                    .to("direct:mediumPriorityProcessor")
                .when(jsonpath("$.priority[?(@ == 'LOW')]"))
                    .log("Processing LOW priority order")
                    .to("direct:lowPriorityProcessor")
                .otherwise()
                    .log("Processing DEFAULT priority order")
                    .to("direct:defaultProcessor")
            .end()
            .setBody(constant("Order processed successfully"));

        // Route 2: Dynamic routing based on customer type
        from("jetty:http://0.0.0.0:8080/customers")
            .log("Received customer request: ${body}")
            .choice()
                .when(jsonpath("$.customerType[?(@ == 'PREMIUM')]"))
                    .log("Routing to premium customer service")
                    .to("direct:premiumService")
                .when(jsonpath("$.customerType[?(@ == 'STANDARD')]"))
                    .log("Routing to standard customer service")
                    .to("direct:standardService")
                .otherwise()
                    .log("Routing to default customer service")
                    .to("direct:defaultService")
            .end()
            .setBody(constant("Customer request processed"));

        // Processor routes for orders
        from("direct:highPriorityProcessor")
            .log("HIGH Priority: Expedited processing")
            .setHeader("Priority", constant("HIGH"));

        from("direct:mediumPriorityProcessor")
            .log("MEDIUM Priority: Standard processing")
            .setHeader("Priority", constant("MEDIUM"));

        from("direct:lowPriorityProcessor")
            .log("LOW Priority: Batch processing")
            .setHeader("Priority", constant("LOW"));

        from("direct:defaultProcessor")
            .log("DEFAULT Priority: Standard processing")
            .setHeader("Priority", constant("DEFAULT"));

        // Service routes for customers
        from("direct:premiumService")
            .log("Premium Service: VIP treatment")
            .setHeader("ServiceLevel", constant("PREMIUM"));

        from("direct:standardService")
            .log("Standard Service: Regular treatment")
            .setHeader("ServiceLevel", constant("STANDARD"));

        from("direct:defaultService")
            .log("Default Service: Standard treatment")
            .setHeader("ServiceLevel", constant("DEFAULT"));
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.configure().addRoutesBuilder(new DynamicRoutingExample());
        main.run(args);
    }
}
