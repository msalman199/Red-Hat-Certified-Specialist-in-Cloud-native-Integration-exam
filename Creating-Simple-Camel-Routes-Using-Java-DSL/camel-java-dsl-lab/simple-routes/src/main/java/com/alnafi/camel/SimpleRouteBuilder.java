package com.alnafi.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

public class SimpleRouteBuilder extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        
        // Route 1: Timer to File with Processing
        from("timer:hello?period=5000")
            .process(new MessageProcessor())
            .to("file:output?fileName=processed-message-${date:now:yyyyMMdd-HHmmss}.txt");
        
        // Route 2: File to File with Transformation
        from("file:input?noop=true")
            .process(new FileProcessor())
            .to("file:processed");
        
        // Route 3: Simple transformation route
        from("timer:simple?period=10000")
            .process(exchange -> {
                String originalMessage = "Simple message from timer";
                String transformedMessage = originalMessage.toUpperCase() + 
                    " - Processed at: " + new java.util.Date();
                exchange.getIn().setBody(transformedMessage);
            })
            .to("file:simple-output?fileName=simple-${date:now:HHmmss}.txt");
    }
    
    // Custom Processor Class
    private static class MessageProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            // Get the current timestamp
            String timestamp = new java.util.Date().toString();
            
            // Create a custom message
            String message = "Hello from Apache Camel!\n" +
                           "Route processed at: " + timestamp + "\n" +
                           "Exchange ID: " + exchange.getExchangeId() + "\n" +
                           "This message was created using Java DSL\n";
            
            // Set the processed message as the body
            exchange.getIn().setBody(message);
            
            // Add a custom header
            exchange.getIn().setHeader("ProcessedBy", "MessageProcessor");
            exchange.getIn().setHeader("ProcessingTime", timestamp);
            
            System.out.println("Message processed: " + message.substring(0, 50) + "...");
        }
    }
    
    // File Processor Class
    private static class FileProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            // Get the original file content
            String originalContent = exchange.getIn().getBody(String.class);
            String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
            
            // Process the content
            String processedContent = "=== FILE PROCESSING REPORT ===\n" +
                                    "Original File: " + fileName + "\n" +
                                    "Processing Time: " + new java.util.Date() + "\n" +
                                    "Content Length: " + originalContent.length() + " characters\n" +
                                    "=== ORIGINAL CONTENT ===\n" +
                                    originalContent + "\n" +
                                    "=== END OF PROCESSING ===\n";
            
            // Set the processed content
            exchange.getIn().setBody(processedContent);
            
            // Update filename
            exchange.getIn().setHeader(Exchange.FILE_NAME, "processed-" + fileName);
            
            System.out.println("File processed: " + fileName);
        }
    }
}
