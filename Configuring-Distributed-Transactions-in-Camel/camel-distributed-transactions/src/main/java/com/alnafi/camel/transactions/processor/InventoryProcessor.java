package com.alnafi.camel.transactions.processor;

import com.alnafi.camel.transactions.model.Order;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class InventoryProcessor implements Processor {

    @Autowired
    private DataSource xaDataSource;

    @Override
    public void process(Exchange exchange) throws Exception {
        Order order = exchange.getIn().getBody(Order.class);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(xaDataSource);
        
        // Check inventory availability
        String sql = "SELECT available_quantity FROM inventory WHERE product_name = ?";
        
        try {
            Integer availableQuantity = jdbcTemplate.queryForObject(sql, Integer.class, order.getProductName());
            
            boolean inventoryAvailable = availableQuantity != null && availableQuantity >= order.getQuantity();
            exchange.getIn().setHeader("inventoryAvailable", inventoryAvailable);
            exchange.getIn().setHeader("availableQuantity", availableQuantity);
            
            if (!inventoryAvailable) {
                exchange.getIn().setHeader("inventoryError", 
                    String.format("Insufficient inventory. Available: %d, Requested: %d", 
                        availableQuantity != null ? availableQuantity : 0, order.getQuantity()));
            }
        } catch (Exception e) {
            exchange.getIn().setHeader("inventoryAvailable", false);
            exchange.getIn().setHeader("inventoryError", "Product not found in inventory");
        }
    }
}
