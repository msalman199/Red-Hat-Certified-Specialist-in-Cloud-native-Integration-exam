package com.example.camel;

import org.apache.camel.main.Main;

public class CamelRestApplication {
    
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        
        // Add our route
        main.addRouteBuilder(new UserRestRoute());
        
        // Configure main
        main.configure().setDurationMaxMessages(0);
        main.configure().setShutdownTimeout(10);
        
        System.out.println("Starting Camel REST API...");
        System.out.println("API will be available at: http://localhost:8080/api/users");
        System.out.println("Press Ctrl+C to stop the application");
        
        // Start and keep running
        main.run(args);
    }
}
