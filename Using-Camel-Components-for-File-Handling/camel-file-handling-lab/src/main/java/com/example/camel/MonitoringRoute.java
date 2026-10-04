package com.example.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

public class MonitoringRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        
        // File processing with retry and dead letter queue
        from("file:monitored-input?noop=false&move=monitored-processed")
            .routeId("monitored-file-processing")
            .onException(Exception.class)
                .maximumRedeliveries(3)
                .redeliveryDelay(2000)
                .retryAttemptedLogLevel(org.apache.camel.LoggingLevel.WARN)
                .to("file:dead-letter-queue")
                .handled(true)
            .end()
            .log("Processing monitored file: ${header.CamelFileName}")
            .process(new FileValidationProcessor())
            .to("file:validated-output")
            .log("File validated and processed: ${header.CamelFileName}");
        
        // Statistics and monitoring route
        from("timer://stats?period=30000")
            .routeId("statistics-route")
            .process(new StatisticsProcessor())
            .log("${body}");
    }
    
    private static class FileValidationProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            String content = exchange.getIn().getBody(String.class);
            String filename = exchange.getIn().getHeader("CamelFileName", String.class);
            
            // Simulate validation logic
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("File is empty: " + filename);
            }
            
            if (filename.contains("invalid")) {
                throw new IllegalArgumentException("Invalid filename pattern: " + filename);
            }
            
            // Add validation timestamp
            exchange.getIn().setHeader("ValidationTimestamp", 
                java.time.LocalDateTime.now().toString());
        }
    }
    
    private static class StatisticsProcessor implements Processor {
        @Override
        public void process(Exchange exchange) throws Exception {
            // Simple statistics - in real scenario, you'd use proper metrics
            java.io.File inputDir = new java.io.File("input");
            java.io.File outputDir = new java.io.File("output");
            java.io.File processedDir = new java.io.File("processed");
            java.io.File errorDir = new java.io.File("error");
            
            String stats = String.format(
                "File Processing Statistics:\n" +
                "- Input files pending: %d\n" +
                "- Output files created: %d\n" +
                "- Files processed: %d\n" +
                "- Error files: %d\n" +
                "- Timestamp: %s",
                inputDir.listFiles() != null ? inputDir.listFiles().length : 0,
                outputDir.listFiles() != null ? outputDir.listFiles().length : 0,
                processedDir.listFiles() != null ? processedDir.listFiles().length : 0,
                errorDir.listFiles() != null ? errorDir.listFiles().length : 0,
                java.time.LocalDateTime.now()
            );
            
            exchange.getIn().setBody(stats);
        }
    }
}
