package com.alnafi.camel.lab4.routes;

import com.alnafi.camel.lab4.processors.MessageEnrichmentProcessor;
import com.alnafi.camel.lab4.processors.MessageTransformProcessor;
import org.apache.camel.builder.RouteBuilder;

/**
 * Route builder that demonstrates custom processor integration
 */
public class CustomProcessorRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Route 1: Basic message transformation using custom processor
        from("direct:transform")
            .routeId("message-transform-route")
            .log("Received message for transformation: ${body}")
            .process(new MessageTransformProcessor())
            .log("Message after transformation: ${body}")
            .log("Custom headers added: ProcessedBy=${header.ProcessedBy}, "
                 + "Timestamp=${header.ProcessingTimestamp}")
            .to("direct:output");

        // Route 2: Advanced message enrichment using custom processor
        from("direct:enrich")
            .routeId("message-enrichment-route")
            .log("Received message for enrichment: ${body}")
            .log("Message type: ${header.MessageType}")
            .process(new MessageEnrichmentProcessor())
            .log("Message after enrichment: ${body}")
            .log("Enrichment metadata: EnrichedBy=${header.EnrichedBy}, "
                 + "Priority=${header.Priority}")
            .to("direct:output");

        // Route 3: Combined processing - both processors in sequence
        from("direct:combined")
            .routeId("combined-processing-route")
            .log("Starting combined processing for: ${body}")
            .process(new MessageTransformProcessor())
            .log("After transformation: ${body}")
            .process(new MessageEnrichmentProcessor())
            .log("After enrichment: ${body}")
            .log("Final headers: ProcessedBy=${header.ProcessedBy}, "
                 + "EnrichedBy=${header.EnrichedBy}, Priority=${header.Priority}")
            .to("direct:output");

        // Route 4: Timer-based route for automatic testing
        from("timer:autoTest?period=5000&repeatCount=3")
            .routeId("auto-test-route")
            .setBody(constant("Hello from timer route"))
            .setHeader("MessageType", constant("SYSTEM_MESSAGE"))
            .log("Auto-generated message: ${body}")
            .to("direct:transform");

        // Output route - final destination for all processed messages
        from("direct:output")
            .routeId("output-route")
            .log("=== FINAL PROCESSED MESSAGE ===")
            .log("Body: ${body}")
            .log("All Headers: ${headers}")
            .log("=== END OF PROCESSING ===");
    }
}
