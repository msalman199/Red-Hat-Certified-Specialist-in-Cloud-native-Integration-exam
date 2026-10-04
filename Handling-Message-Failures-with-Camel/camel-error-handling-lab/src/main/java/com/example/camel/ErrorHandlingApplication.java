package com.example.camel;

import org.apache.camel.main.Main;

public class ErrorHandlingApplication {

    public static void main(String[] args) throws Exception {
        Main main = new Main();

        main.addRouteBuilder(new RetryRouteBuilder());
        main.addRouteBuilder(new DeadLetterQueueRouteBuilder());
        main.addRouteBuilder(new FallbackRouteBuilder());
        main.addRouteBuilder(new AdvancedErrorHandlingRouteBuilder());

        main.configure().setShutdownTimeout(30);

        System.out.println("Starting Camel Error Handling Application...");
        System.out.println("Press Ctrl+C to stop the application");

        main.run(args);
    }
}
