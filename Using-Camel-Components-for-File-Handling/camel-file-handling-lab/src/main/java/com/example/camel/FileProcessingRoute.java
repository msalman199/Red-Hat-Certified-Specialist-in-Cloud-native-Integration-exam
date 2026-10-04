package com.example.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

public class FileProcessingRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        
        // Basic file consumption and production route
        from("file:input?noop=false&move=processed")
            .routeId("file-processor")
            .log("Processing file: ${header.CamelFileName}")
            .process(new FileContentProcessor())
            .to("file:output")
            .log("File processed successfully: ${header.CamelFileName}");
        
        // Error handling route
        onException(Exception.class)
            .handled(true)
            .log("Error processing file: ${exception.message}")
            .to("file:error");
    }
    
    // Custom processor to transform file content
    private static class FileContentProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            String originalContent = exchange.getIn().getBody(String.class);
            String processedContent = "PROCESSED: " + originalContent.toUpperCase();
            exchange.getIn().setBody(processedContent);
            
            // Add processing timestamp
            exchange.getIn().setHeader("ProcessedTimestamp", 
                java.time.LocalDateTime.now().toString());
        }
    }
}
