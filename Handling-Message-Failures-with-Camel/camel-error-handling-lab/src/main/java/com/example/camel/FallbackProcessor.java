package com.example.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class FallbackProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        String originalBody = exchange.getIn().getBody(String.class);

        String fallbackResult = "FALLBACK_PROCESSED: " + originalBody + " [Processed via fallback mechanism]";

        exchange.getIn().setBody(fallbackResult);
        exchange.getIn().setHeader("ProcessingMethod", "FALLBACK");
        exchange.getIn().setHeader("FallbackTimestamp", System.currentTimeMillis());

        System.out.println("Fallback processing applied for: " + originalBody);
    }
}
