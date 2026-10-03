package com.example.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

public class CompleteTestApplication {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Complete Camel REST API Consumer Test ===");
        System.out.println("This run demonstrates: fetch, transform, validate, store, and monitor.");

        CamelContext camelContext = new DefaultCamelContext();
        camelContext.addRoutes(new EnhancedRestConsumerRoute());
        camelContext.addRoutes(new MonitoringRoute());
        camelContext.start();

        System.out.println("All routes started. Running for 65 seconds...");

        Thread.sleep(65000);

        System.out.println("=== Stopping Application ===");
        System.out.println("Total routes registered: " + camelContext.getRoutes().size());

        camelContext.stop();
        System.out.println("Application stopped successfully.");
    }
}
