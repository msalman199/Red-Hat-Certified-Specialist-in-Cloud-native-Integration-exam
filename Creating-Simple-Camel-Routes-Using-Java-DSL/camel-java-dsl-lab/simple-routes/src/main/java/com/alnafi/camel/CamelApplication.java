package com.alnafi.camel;

import org.apache.camel.main.Main;

public class CamelApplication {
    
    public static void main(String[] args) throws Exception {
        // Create Camel Main instance
        Main main = new Main();
        
        // Add our route builder
        main.addRouteBuilder(new SimpleRouteBuilder());
        
        // Configure Camel context
        main.configure().setName("SimpleRoutesApplication");
        
        // Start the application
        System.out.println("Starting Apache Camel Application...");
        System.out.println("Routes will process messages every 5-10 seconds");
        System.out.println("Check the 'output', 'processed', and 'simple-output' directories for results");
        System.out.println("Press Ctrl+C to stop the application");
        
        // Run the application
        main.run(args);
    }
}
