package com.example.camel;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.common.HttpMethods;

public class EnhancedRestConsumerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
            .handled(true)
            .log("ERROR occurred: ${exception.message}")
            .to("direct:handleError");

        // Route 1: Fetch posts and apply transformation/enrichment
        from("timer://enhancedPosts?period=20000&repeatCount=2")
            .routeId("enhanced-posts-route")
            .log("=== Starting Enhanced Posts Fetch ===")
            .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
            .setHeader("User-Agent", constant("Camel-REST-Consumer/1.0"))
            .setHeader("Accept", constant("application/json"))
            .to("https://jsonplaceholder.typicode.com/posts?_limit=5&bridgeEndpoint=true")
            .log("Posts API Response Code: ${header.CamelHttpResponseCode}")
            .choice()
                .when(header("CamelHttpResponseCode").isEqualTo(200))
                    .process(new DataTransformProcessor())
                    .log("Data transformation completed")
                    .to("direct:validateAndStore")
                .otherwise()
                    .to("direct:handleApiError")
            .end();

        // Route 2: Validate transformed data and store each post
        from("direct:validateAndStore")
            .routeId("validate-store-route")
            .log("Validating transformed data...")
            .split().jsonpath("$[*]")
                .choice()
                    .when(simple("${body[category]} != null"))
                        .log("Storing post: ${body[title]} (Category: ${body[category]}, Words: ${body[wordCount]})")
                        .to("direct:storeInDatabase")
                    .otherwise()
                        .log("Validation failed - post missing category field, skipping")
                .end()
            .end();

        // Route 3: Simulated database storage
        from("direct:storeInDatabase")
            .routeId("store-database-route")
            .log("DATABASE INSERT: ${body[title]}");

        // Route 4: API-level error handler (non-200 responses)
        from("direct:handleApiError")
            .routeId("handle-api-error-route")
            .log("=== API ERROR HANDLER ===")
            .log("Response Code: ${header.CamelHttpResponseCode}")
            .choice()
                .when(header("CamelHttpResponseCode").isEqualTo(404))
                    .log("Resource not found - skipping this cycle")
                .when(header("CamelHttpResponseCode").isEqualTo(429))
                    .log("Rate limit exceeded - backing off for 5 seconds")
                    .delay(5000)
                .otherwise()
                    .log("Unexpected response code encountered")
            .end();

        // Route 5: Exception-level error handler (timeouts, connection errors)
        from("direct:handleError")
            .routeId("general-error-handler-route")
            .log("=== GENERAL ERROR HANDLER ===")
            .log("Exception message: ${exception.message}");
    }
}

