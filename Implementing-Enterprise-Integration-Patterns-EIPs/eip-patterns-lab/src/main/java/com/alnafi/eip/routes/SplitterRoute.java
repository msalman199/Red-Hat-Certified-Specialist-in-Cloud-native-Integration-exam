package com.alnafi.eip.routes;

import com.alnafi.eip.model.BatchOrder;
import com.alnafi.eip.model.CustomerOrder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class SplitterRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        
        // Main splitter route
        from("direct:processBatchOrder")
            .routeId("batch-order-splitter")
            .log("Received batch order: ${body}")
            .unmarshal().json(JsonLibrary.Jackson, BatchOrder.class)
            .process(exchange -> {
                BatchOrder batchOrder = exchange.getIn().getBody(BatchOrder.class);
                System.out.println("=== BATCH ORDER PROCESSING ===");
                System.out.println("Batch ID: " + batchOrder.getBatchId());
                System.out.println("Total Orders: " + batchOrder.getTotalOrders());
                System.out.println("Starting to split batch...");
                System.out.println("==============================");
            })
            // Split the batch into individual orders
            .split(simple("${body.orders}"))
                .streaming() // Process one at a time to save memory
                .log("Processing individual order from batch: ${body.orderId}")
                .process(exchange -> {
                    CustomerOrder order = exchange.getIn().getBody(CustomerOrder.class);
                    System.out.println("--- Individual Order Processing ---");
                    System.out.println("Order ID: " + order.getOrderId());
                    System.out.println("Customer Type: " + order.getCustomerType());
                    System.out.println("Amount: $" + order.getAmount());
                    System.out.println("----------------------------------");
                })
                // Send each individual order to the Content-Based Router
                .marshal().json(JsonLibrary.Jackson)
                .to("direct:processOrder")
            .end()
            .log("Batch processing completed");
        
        // Route to demonstrate aggregation after splitting (optional)
        from("direct:processBatchWithAggregation")
            .routeId("batch-splitter-with-aggregation")
            .log("Processing batch with aggregation: ${body}")
            .unmarshal().json(JsonLibrary.Jackson, BatchOrder.class)
            .split(simple("${body.orders}"))
                .streaming()
                .log("Processing and aggregating order: ${body.orderId}")
                .process(exchange -> {
                    CustomerOrder order = exchange.getIn().getBody(CustomerOrder.class);
                    // Simulate processing time
                    Thread.sleep(100);
                    
                    // Add processing result to order
                    System.out.println("Processed order: " + order.getOrderId() + 
                                     " for customer type: " + order.getCustomerType());
                })
                // Aggregate results back together
                .aggregate(constant(true))
                    .strategy((oldExchange, newExchange) -> {
                        if (oldExchange == null) {
                            return newExchange;
                        }
                        
                        String oldBody = oldExchange.getIn().getBody(String.class);
                        String newBody = newExchange.getIn().getBody(String.class);
                        
                        oldExchange.getIn().setBody(oldBody + "," + newBody);
                        return oldExchange;
                    })
                    .completionTimeout(5000) // Complete after 5 seconds
            .end()
            .log("Aggregated results: ${body}");
    }
}
