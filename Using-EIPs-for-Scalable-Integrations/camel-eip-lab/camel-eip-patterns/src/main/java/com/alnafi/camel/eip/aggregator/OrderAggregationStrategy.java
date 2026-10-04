package com.alnafi.camel.eip.aggregator;

import com.alnafi.camel.eip.splitter.OrderItem;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import java.util.ArrayList;

public class OrderAggregationStrategy implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        OrderItem newItem = newExchange.getIn().getBody(OrderItem.class);
        Double itemTotal = newExchange.getIn().getHeader("itemTotal", Double.class);

        if (oldExchange == null) {
            AggregatedOrder aggregatedOrder = new AggregatedOrder();
            aggregatedOrder.setOrderId(newExchange.getIn().getHeader("correlationId", String.class));
            aggregatedOrder.setItems(new ArrayList<>());
            aggregatedOrder.getItems().add(newItem);
            aggregatedOrder.setTotalAmount(itemTotal);
            aggregatedOrder.setItemCount(1);

            newExchange.getIn().setBody(aggregatedOrder);
            return newExchange;
        } else {
            AggregatedOrder aggregatedOrder = oldExchange.getIn().getBody(AggregatedOrder.class);
            aggregatedOrder.getItems().add(newItem);
            aggregatedOrder.setTotalAmount(aggregatedOrder.getTotalAmount() + itemTotal);
            aggregatedOrder.setItemCount(aggregatedOrder.getItemCount() + 1);

            return oldExchange;
        }
    }
}
