package com.alnafi.camel.errorhandling;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RetryTestApplication {

    public static void main(String[] args) throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.addRoutes(new ErrorHandlingRouteBuilder());

        Files.createDirectories(Paths.get("output/success"));
        Files.createDirectories(Paths.get("output/validation-errors"));
        Files.createDirectories(Paths.get("output/failures"));

        context.start();

        ProducerTemplate template = context.createProducerTemplate();

        System.out.println("=== Testing Retry Logic ===");

        System.out.println("\n1. Testing message that succeeds after retries...");
        template.sendBody("direct:process-message", "RETRY_SUCCESS - This will fail twice then succeed");
        Thread.sleep(6000);

        System.out.println("\n2. Testing validation error (no retries)...");
        template.sendBody("direct:process-message", "VALIDATION_ERROR - Invalid format");
        Thread.sleep(1000);

        System.out.println("\n3. Testing successful message...");
        template.sendBody("direct:process-message", "SUCCESS - This message will process normally");
        Thread.sleep(1000);

        context.stop();
        System.out.println("Test completed. Check the output directory for results.");
    }
}
