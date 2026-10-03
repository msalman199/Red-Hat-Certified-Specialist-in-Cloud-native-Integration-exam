package com.alnafi.kafka.lab;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

public class KafkaCamelApplication {
    
    public static void main(String[] args) throws Exception {
        System.out.println("Starting Kafka-Camel Integration Application...");
        
        // Create Camel Context
        CamelContext camelContext = new DefaultCamelContext();
        
        try {
            // Add routes
            camelContext.addRoutes(new OrderProducerRoute());
            camelContext.addRoutes(new OrderConsumerRoute());
            
            // Start the context
            camelContext.start();
            
            System.out.println("Application started successfully!");
            System.out.println("Producer will generate orders every 5 seconds");
            System.out.println("Consumers are listening for messages");
            System.out.println("Press Ctrl+C to stop the application");
            
            // Keep the application running
            Thread.sleep(Long.MAX_VALUE);
            
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Stop the context
            camelContext.stop();
            System.out.println("Application stopped.");
        }
    }
}
