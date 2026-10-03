package com.example.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

public class RestConsumerApplication {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Starting Camel REST API Consumer ===");

        CamelContext camelContext = new DefaultCamelContext();
        camelContext.addRoutes(new EnhancedRestConsumerRoute());
        camelContext.start();

        System.out.println("Enhanced Camel REST Consumer started. Running for 60 seconds...");

        Thread.sleep(60000);

        System.out.println("=== Stopping Camel REST API Consumer ===");
        camelContext.stop();
        System.out.println("Application stopped successfully.");
    }
}
