package com.alnafi.integration.processor;

import com.alnafi.integration.model.Order;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class CustomerEnrichmentProcessor implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(CustomerEnrichmentProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        Order order = exchange.getIn().getBody(Order.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> customerData =
            (List<Map<String, Object>>) exchange.getProperty("customerData");

        if (customerData != null && !customerData.isEmpty()) {
            Map<String, Object> customer = customerData.get(0);
            order.setCustomerName((String) customer.get("customer_name"));
            order.setCustomerEmail((String) customer.get("email"));
            logger.info("Enriched order {} with customer: {}", order.getOrderNumber(), order.getCustomerName());
        } else {
            logger.warn("No customer found for code: {}", order.getCustomerCode());
            throw new IllegalStateException("Customer not found: " + order.getCustomerCode());
        }

        exchange.getIn().setBody(order);
    }
}
