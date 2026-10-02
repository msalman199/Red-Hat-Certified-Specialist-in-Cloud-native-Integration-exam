package com.alnafi.camel.errorhandling;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DLQTestApplication {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new DLQRouteBuilder());

        Files.createDirectories(Paths.get("output/success"));
        Files.createDirectories(Paths.get("output/dlq/runtime-errors"));
        Files.createDirectories(Paths.get("output/dlq/validation-errors"));

        context.start();

        ProducerTemplate template = context.createProducerTemplate();

        System.out.println("=== Testing Dead Letter Queue Functionality ===");

        System.out.println("\n1. Testing message that goes to DLQ after retries...");
        template.sendBody("direct:input.messages", "NETWORK_ERROR - This will fail and go to DLQ");
        Thread.sleep(4000);

        System.out.println("\n2. Testing validation error (immediate DLQ)...");
        template.sendBody("direct:input.messages", "VALIDATION_ERROR - Invalid format");
        Thread.sleep(1000);

        System.out.println("\n3. Testing successful message...");
        template.sendBody("direct:input.messages", "SUCCESS - This will process normally");
        Thread.sleep(1000);

        System.out.println("\n4. Sending a batch of mixed messages...");
        for (int i = 1; i <= 4; i++) {
            if (i % 2 == 0) {
                template.sendBody("direct:input.messages", "SUCCESS - Message " + i);
            } else {
                template.sendBody("direct:input.messages", "NETWORK_ERROR - Failed message " + i);
            }
            Thread.sleep(500);
        }
        Thread.sleep(4000);

        displayResults();

        context.stop();
        System.out.println("\nDLQ test completed. Check output directories for detailed results.");
    }

    private static void displayResults() throws Exception {
        System.out.println("\n=== Processing Results ===");
        System.out.println("Success files: " +
            Files.list(Paths.get("output/success")).count());
        System.out.println("Runtime error DLQ files: " +
            Files.list(Paths.get("output/dlq/runtime-errors")).count());
        System.out.println("Validation error DLQ files: " +
            Files.list(Paths.get("output/dlq/validation-errors")).count());
    }
}
