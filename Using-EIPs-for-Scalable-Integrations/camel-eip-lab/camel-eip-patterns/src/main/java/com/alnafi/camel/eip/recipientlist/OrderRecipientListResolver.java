package com.alnafi.camel.eip.recipientlist;

import com.alnafi.camel.eip.aggregator.AggregatedOrder;
import org.apache.camel.Exchange;
import java.util.ArrayList;
import java.util.List;

public class OrderRecipientListResolver {

    public static String resolveRecipients(Exchange exchange) {
        AggregatedOrder order = exchange.getIn().getBody(AggregatedOrder.class);
        List<String> recipients = new ArrayList<>();

        // Always send to order processing
        recipients.add("direct:orderProcessing");

        // Send to shipping for orders over $50
        if (order.getTotalAmount() > 50) {
            recipients.add("direct:shippingArrangement");
        }

        // Send to finance for orders over $500
        if (order.getTotalAmount() > 500) {
            recipients.add("direct:financeApproval");
        }

        // Always notify the customer
        recipients.add("direct:customerNotification");

        String recipientList = String.join(",", recipients);
        System.out.println("Recipients for order " + order.getOrderId() + ": " + recipientList);

        return recipientList;
    }
}
