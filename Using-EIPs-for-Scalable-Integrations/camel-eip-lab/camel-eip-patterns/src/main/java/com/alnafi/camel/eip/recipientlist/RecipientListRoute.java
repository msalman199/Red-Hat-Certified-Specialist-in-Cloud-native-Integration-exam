package com.alnafi.camel.eip.recipientlist;

import org.apache.camel.builder.RouteBuilder;

public class RecipientListRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Recipient List - dynamically routes the finalized order to multiple endpoints
        from("direct:sendToRecipients")
            .routeId("recipient-list-router")
            .log("Determining recipients for order: ${body.orderId}")
            .recipientList(method(OrderRecipientListResolver.class, "resolveRecipients"))
                .parallelProcessing()
                .stopOnException()
                .timeout(10000)
            .end()
            .log("Message sent to all recipients for order: ${body.orderId}");

        from("direct:orderProcessing")
            .routeId("order-processing-system")
            .log("ORDER PROCESSING: Processing order ${body.orderId}");

        from("direct:shippingArrangement")
            .routeId("shipping-arrangement-system")
            .log("SHIPPING: Arranging shipping for order ${body.orderId}");

        from("direct:financeApproval")
            .routeId("finance-approval-system")
            .log("FINANCE: Processing finance approval for order ${body.orderId}");

        from("direct:customerNotification")
            .routeId("customer-notification-system")
            .log("NOTIFICATION: Sending notification for order ${body.orderId}");
    }
}
