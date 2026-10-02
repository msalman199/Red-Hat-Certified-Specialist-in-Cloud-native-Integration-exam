package com.alnafi.camel.xmldsl;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component("myAggregationStrategy")
public class MyAggregationStrategy implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            // First message in the aggregation
            newExchange.getIn().setBody("[" + newExchange.getIn().getBody(String.class) + "]");
            return newExchange;
        }

        // Subsequent messages - append to the array
        String oldBody = oldExchange.getIn().getBody(String.class);
        String newBody = newExchange.getIn().getBody(String.class);
        
        // Remove the closing bracket and add the new message
        String aggregatedBody = oldBody.substring(0, oldBody.length() - 1) + "," + newBody + "]";
        
        oldExchange.getIn().setBody(aggregatedBody);
        return oldExchange;
    }
}
