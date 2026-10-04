package com.alnafi.camel.eip.aggregator;

import org.apache.camel.builder.RouteBuilder;

public class AggregatorRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Aggregator route - combines split items back into a complete order
        from("direct:aggregateItems")
            .routeId("item-aggregator")
            .log("Aggregating item: ${header.itemId} for order: ${header.correlationId}")
            .aggregate(header("correlationId"), new OrderAggregationStrategy())
                .completionTimeout(3000)
                .completionSize(4)
                .log("Aggregation completed for order: ${header.correlationId}")
                .process(exchange -> {
                    AggregatedOrder order = exchange.getIn().getBody(AggregatedOrder.class);
                    order.setAggregationEndTime(System.currentTimeMillis());

                    log.info("Order aggregation completed: {}", order);
                    log.info("Items aggregated: {}", order.getItemCount());
                    log.info("Total amount: {}", order.getTotalAmount());
                })
                .to("direct:finalizeOrder")
            .end();

        // Final order processing route - applies simple business rules
        from("direct:finalizeOrder")
            .routeId("order-finalizer")
            .log("Finalizing aggregated order: ${body.orderId}")
            .process(exchange -> {
                AggregatedOrder order = exchange.getIn().getBody(AggregatedOrder.class);

                if (order.getTotalAmount() > 1000) {
                    double discount = order.getTotalAmount() * 0.10;
                    order.setTotalAmount(order.getTotalAmount() - discount);
                    log.info("Applied 10% discount: {}", discount);
                }

                double tax = order.getTotalAmount() * 0.08;
                order.setTotalAmount(order.getTotalAmount() + tax);

                log.info("Final order total with tax: {}", order.getTotalAmount());
            })
            .to("direct:sendToRecipients");
    }
}
