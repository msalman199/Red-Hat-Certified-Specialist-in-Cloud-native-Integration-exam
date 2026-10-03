package com.alnafi.camel.transactions.processor;

import com.alnafi.camel.transactions.model.Order;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Order order = exchange.getIn().getBody(Order.class);
        
        // Validate order
        boolean isValid = true;
        String validationError = null;
        
        if (order.getCustomerName() == null || order.getCustomerName().trim().isEmpty()) {
            isValid = false;
            validationError = "Customer name is required";
        } else if (order.getProductName() == null || order.getProductName().trim().isEmpty()) {
            isValid = false;
            validationError = "Product name is required";
        } else if (order.getQuantity() == null || order.getQuantity() <= 0) {
            isValid = false;
            validationError = "Quantity must be greater than 0";
        } else if (order.getPrice() == null || order.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            isValid = false;
            validationError = "Price must be greater than 0";
        }
        
        exchange.getIn().setHeader("orderValid", isValid);
        if (!isValid) {
            exchange.getIn().setHeader("validationError", validationError);
        }
        
        // Set order ID for tracking
        if (order.getId() == null) {
            order.setId(System.currentTimeMillis());
        }
    }
}
