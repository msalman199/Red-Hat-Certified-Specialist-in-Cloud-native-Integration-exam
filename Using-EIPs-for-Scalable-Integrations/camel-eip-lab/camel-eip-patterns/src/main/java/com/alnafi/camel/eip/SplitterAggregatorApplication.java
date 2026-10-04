package com.alnafi.camel.eip;

import com.alnafi.camel.eip.aggregator.AggregatorRoute;
import com.alnafi.camel.eip.splitter.SplitterRoute;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

public class SplitterAggregatorApplication {

    public static void main(String[] args) throws Exception {
        Main main = new Main();

        main.addRouteBuilder(new SplitterRoute());
        main.addRouteBuilder(new AggregatorRoute());

        // Temporary sink so this demo can run without the recipient list (Task 3)
        main.addRouteBuilder(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:sendToRecipients")
                    .routeId("temporary-sink")
                    .log("Final order ready for dispatch: ${body}");
            }
        });

        main.configure().setName("SplitterAggregatorEIPDemo");

        System.out.println("Starting Splitter-Aggregator EIP Demo...");
        System.out.println("Generates 2 sample orders, splits each into 4 items, then aggregates them back.");
        System.out.println("Press Ctrl+C to stop the application.");

        main.run(args);
    }
}
