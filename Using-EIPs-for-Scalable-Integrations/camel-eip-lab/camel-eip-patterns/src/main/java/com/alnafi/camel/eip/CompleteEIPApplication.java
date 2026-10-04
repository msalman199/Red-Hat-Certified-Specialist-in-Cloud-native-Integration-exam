package com.alnafi.camel.eip;

import com.alnafi.camel.eip.aggregator.AggregatorRoute;
import com.alnafi.camel.eip.recipientlist.RecipientListRoute;
import com.alnafi.camel.eip.splitter.SplitterRoute;
import org.apache.camel.main.Main;

public class CompleteEIPApplication {

    public static void main(String[] args) throws Exception {
        Main main = new Main();

        main.addRouteBuilder(new SplitterRoute());
        main.addRouteBuilder(new AggregatorRoute());
        main.addRouteBuilder(new RecipientListRoute());

        main.configure().setName("CompleteEIPDemo");

        System.out.println("Starting Complete EIP Demo Application");
        System.out.println("This demonstrates three Enterprise Integration Patterns:");
        System.out.println("1. SPLITTER: Divides orders into individual items");
        System.out.println("2. AGGREGATOR: Combines processed items back into complete orders");
        System.out.println("3. RECIPIENT LIST: Routes finalized orders to multiple systems");
        System.out.println("Order Generation -> Splitting -> Processing -> Aggregation -> Distribution");
        System.out.println("Press Ctrl+C to stop the application.");

        main.run(args);
    }
}
