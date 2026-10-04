// File: src/main/java/com/example/routing/AdvancedDynamicRouting.java
package com.example.routing;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AdvancedDynamicRouting extends RouteBuilder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure() throws Exception {

        // Route 1: Category-based dynamic routing using ConfigurationService
        from("jetty:http://0.0.0.0:8080/products")
            .log("Received product order: ${body}")
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                JsonNode json = objectMapper.readTree(body);
                String category = json.get("category").asText();
                String targetRoute = ConfigurationService.getRouteForCategory(category);
                exchange.getIn().setHeader("targetRoute", targetRoute);
                exchange.getIn().setHeader("category", category);
            })
            .recipientList(header("targetRoute"))
            .setBody(constant("Product order routed successfully"));

        // Route 2: Multi-criteria dynamic routing using ConfigurationService
        from("jetty:http://0.0.0.0:8080/complex-orders")
            .log("Received complex order: ${body}")
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                JsonNode json = objectMapper.readTree(body);

                String priority = json.get("priority").asText();
                double amount = json.get("amount").asDouble();
                String region = json.get("region").asText();
                String category = json.get("category").asText();

                exchange.getIn().setHeader("priorityWeight",
                    ConfigurationService.getPriorityWeight(priority));
                exchange.getIn().setHeader("isHighValue",
                    ConfigurationService.isHighValueOrder(amount));
                exchange.getIn().setHeader("regionQueue",
                    ConfigurationService.getProcessingQueue(region));
                exchange.getIn().setHeader("categoryRoute",
                    ConfigurationService.getRouteForCategory(category));
            })
            .choice()
                .when(header("isHighValue").isEqualTo(true))
                    .log("High value order - routing to premium processing")
                    .to("direct:premiumProcessing")
                .when(header("priorityWeight").isLessThan(3))
                    .log("High priority order - routing to express processing")
                    .to("direct:expressProcessing")
                .otherwise()
                    .log("Standard order - routing based on region and category")
                    .recipientList(simple("${header.regionQueue},${header.categoryRoute}"))
            .end()
            .setBody(constant("Complex order processed"));

        // Category processors
        from("direct:electronicsProcessor")
            .log("Processing electronics order")
            .setHeader("ProcessingDepartment", constant("Electronics"));

        from("direct:clothingProcessor")
            .log("Processing clothing order")
            .setHeader("ProcessingDepartment", constant("Clothing"));

        from("direct:booksProcessor")
            .log("Processing books order")
            .setHeader("ProcessingDepartment", constant("Books"));

        from("direct:foodProcessor")
            .log("Processing food order")
            .setHeader("ProcessingDepartment", constant("Food"));

        from("direct:defaultCategoryProcessor")
            .log("Processing general order")
            .setHeader("ProcessingDepartment", constant("General"));

        // Priority processors
        from("direct:premiumProcessing")
            .log("Premium processing for high-value order")
            .setHeader("ProcessingType", constant("Premium"));

        from("direct:expressProcessing")
            .log("Express processing for high-priority order")
            .setHeader("ProcessingType", constant("Express"));

        // Regional queues
        from("direct:northRegionQueue")
            .log("Processing in North region")
            .setHeader("ProcessingRegion", constant("North"));

        from("direct:southRegionQueue")
            .log("Processing in South region")
            .setHeader("ProcessingRegion", constant("South"));

        from("direct:eastRegionQueue")
            .log("Processing in East region")
            .setHeader("ProcessingRegion", constant("East"));

        from("direct:westRegionQueue")
            .log("Processing in West region")
            .setHeader("ProcessingRegion", constant("West"));

        from("direct:defaultRegionQueue")
            .log("Processing in default region")
            .setHeader("ProcessingRegion", constant("Central"));
    }

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.configure().addRoutesBuilder(new AdvancedDynamicRouting());
        main.run(args);
    }
}
