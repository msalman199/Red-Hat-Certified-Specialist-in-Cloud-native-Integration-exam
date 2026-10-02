package com.alnafi.camel;

import org.apache.camel.builder.RouteBuilder;

public class EnhancedRouteBuilder extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        
        // Global error handler
        errorHandler(deadLetterChannel("file:error")
            .maximumRedeliveries(3)
            .redeliveryDelay(1000));
        
        // Enhanced route with error handling
        from("file:input-enhanced?noop=true")
            .onException(Exception.class)
                .handled(true)
                .log("Error processing file: ${exception.message}")
                .to("file:error?fileName=error-${date:now:yyyyMMdd-HHmmss}.txt")
            .end()
            .log("Processing file: ${header.CamelFileName}")
            .process(exchange -> {
                String content = exchange.getIn().getBody(String.class);
                if (content == null || content.trim().isEmpty()) {
                    throw new IllegalArgumentException("Empty file content");
                }
                exchange.getIn().setBody("ENHANCED: " + content);
            })
            .to("file:enhanced-output");
    }
}
