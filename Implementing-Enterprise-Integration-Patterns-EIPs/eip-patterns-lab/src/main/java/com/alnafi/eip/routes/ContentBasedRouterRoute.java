package com.alnafi.eip.routes;

import com.alnafi.eip.model.CustomerOrder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class ContentBasedRouterRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        
        // Main route that receives orders and routes them based on content
        from("direct:processOrder")
            .routeId("content-based-router")
            .log("Received order: ${body}")
            .unmarshal().json(JsonLibrary.Jackson, CustomerOrder.class)
            .choice()
                // Route 1: Premium customers with high-value orders
                .when(simple("${body.customerType} == 'PREMIUM' && ${body.amount} > 1000"))
                    .log("Routing to Premium High-Value queue: ${body.orderId}")
                    .to("direct:premiumHighValue")
                // Route 2: Premium customers with regular orders
                .when(simple("${body.customerType} == 'PREMIUM'"))
                    .log("Routing to Premium Regular queue: ${body.orderId}")
                    .to("direct:premiumRegular")
                // Route 3: High priority orders regardless of customer type
                .when(simple("${body.priority} == 'HIGH'"))
                    .log("Routing to High Priority queue: ${body.orderId}")
                    .to("direct:highPriority")
                // Route 4: Electronics category orders
                .when(simple("${body.productCategory} == 'ELECTRONICS'"))
                    .log("Routing to Electronics queue: ${body.orderId}")
                    .to("direct:electronics")
                // Default route for all other orders
                .otherwise()
                    .log("Routing to Standard queue: ${body.orderId}")
                    .to("direct:standard")
            .end();
        
        // Destination routes for different order types
        from("direct:premiumHighValue")
            .routeId("premium-high-value-processor")
            .log("Processing Premium High-Value order: ${body.orderId}")
            .process(exchange -> {
                CustomerOrder order = exchange.getIn().getBody(CustomerOrder.class);
                System.out.println("*** PREMIUM HIGH-VALUE PROCESSING ***");
                System.out.println("Order ID: " + order.getOrderId());
                System.out.println("Amount: $" + order.getAmount());
                System.out.println("Applying 15% discount and priority shipping");
                System.out.println("*************************************");
            });
        
        from("direct:premiumRegular")
            .routeId("premium-regular-processor")
            .log("Processing Premium Regular order: ${body.orderId}")
            .process(exchange -> {
                CustomerOrder order = exchange.getIn().getBody(CustomerOrder.class);
                System.out.println("*** PREMIUM REGULAR PROCESSING ***");
                System.out.println("Order ID: " + order.getOrderId());
                System.out.println("Applying 10% discount");
                System.out.println("**********************************");
            });
        
        from("direct:highPriority")
            .routeId("high-priority-processor")
            .log("Processing High Priority order: ${body.orderId}")
            .process(exchange -> {
                CustomerOrder order = exchange.getIn().getBody(CustomerOrder.class);
                System.out.println("*** HIGH PRIORITY PROCESSING ***");
                System.out.println("Order ID: " + order.getOrderId());
                System.out.println("Express processing initiated");
                System.out.println("********************************");
            });
        
        from("direct:electronics")
            .routeId("electronics-processor")
            .log("Processing Electronics order: ${body.orderId}")
            .process(exchange -> {
                CustomerOrder order = exchange.getIn().getBody(CustomerOrder.class);
                System.out.println("*** ELECTRONICS PROCESSING ***");
                System.out.println("Order ID: " + order.getOrderId());
                System.out.println("Extended warranty offered");
                System.out.println("******************************");
            });
        
        from("direct:standard")
            .routeId("standard-processor")
            .log("Processing Standard order: ${body.orderId}")
            .process(exchange -> {
                CustomerOrder order = exchange.getIn().getBody(CustomerOrder.class);
                System.out.println("*** STANDARD PROCESSING ***");
                System.out.println("Order ID: " + order.getOrderId());
                System.out.println("Standard processing applied");
                System.out.println("***************************");
            });
    }
}
