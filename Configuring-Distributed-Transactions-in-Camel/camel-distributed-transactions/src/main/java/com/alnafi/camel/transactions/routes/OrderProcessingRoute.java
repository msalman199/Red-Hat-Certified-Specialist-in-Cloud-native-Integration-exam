package com.alnafi.camel.transactions.routes;

import com.alnafi.camel.transactions.model.Order;
import com.alnafi.camel.transactions.processor.InventoryProcessor;
import com.alnafi.camel.transactions.processor.OrderProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderProcessingRoute extends RouteBuilder {

    @Autowired
    private OrderProcessor orderProcessor;

    @Autowired
    private InventoryProcessor inventoryProcessor;

    @Override
    public void configure() throws Exception {

        // Main order processing route with distributed transaction
        from("jms:queue:orders")
            .routeId("orderProcessingRoute")
            .log("Received order: ${body}")
            .transacted("atomikosTransactionManager")
            .unmarshal().json(JsonLibrary.Jackson, Order.class)
            .to("direct:validateOrder")
            .to("direct:processOrder")
            .to("direct:updateInventory")
            .to("direct:sendConfirmation")
            .log("Order processed successfully: ${body}");

        // Order validation route
        from("direct:validateOrder")
            .routeId("validateOrderRoute")
            .log("Validating order: ${body}")
            .process(orderProcessor)
            .choice()
                .when(header("orderValid").isEqualTo(false))
                    .log("Order validation failed: ${header.validationError}")
                    .throwException(new IllegalArgumentException("Order validation failed"))
                .otherwise()
                    .log("Order validation passed");

        // Order processing route
        from("direct:processOrder")
            .routeId("processOrderRoute")
            .log("Processing order in database")
            .setHeader("orderId", simple("${body.id}"))
            .to("sql:INSERT INTO orders (customer_name, product_name, quantity, price, status) " +
                "VALUES (:#${body.customerName}, :#${body.productName}, :#${body.quantity}, :#${body.price}, 'PROCESSING')" +
                "?dataSource=#xaDataSource")
            .to("sql:INSERT INTO audit_log (operation, table_name, details, transaction_id) " +
                "VALUES ('INSERT', 'orders', 'Order created', '${exchangeId}')" +
                "?dataSource=#xaDataSource")
            .log("Order saved to database");

        // Inventory update route
        from("direct:updateInventory")
            .routeId("updateInventoryRoute")
            .log("Updating inventory for product: ${body.productName}")
            .process(inventoryProcessor)
            .choice()
                .when(header("inventoryAvailable").isEqualTo(false))
                    .log("Insufficient inventory for product: ${body.productName}")
                    .to("sql:INSERT INTO audit_log (operation, table_name, details, transaction_id) " +
                        "VALUES ('ERROR', 'inventory', 'Insufficient inventory', '${exchangeId}')" +
                        "?dataSource=#xaDataSource")
                    .throwException(new RuntimeException("Insufficient inventory"))
                .otherwise()
                    .to("sql:UPDATE inventory SET available_quantity = available_quantity - :#${body.quantity}, " +
                        "reserved_quantity = reserved_quantity + :#${body.quantity} " +
                        "WHERE product_name = :#${body.productName}" +
                        "?dataSource=#xaDataSource")
                    .to("sql:INSERT INTO audit_log (operation, table_name, details, transaction_id) " +
                        "VALUES ('UPDATE', 'inventory', 'Inventory updated', '${exchangeId}')" +
                        "?dataSource=#xaDataSource")
                    .log("Inventory updated successfully");

        // Confirmation route
        from("direct:sendConfirmation")
            .routeId("sendConfirmationRoute")
            .log("Sending order confirmation")
            .setBody(simple("Order confirmed for customer: ${body.customerName}, Product: ${body.productName}"))
            .to("jms:queue:confirmations")
            .to("sql:UPDATE orders SET status = 'CONFIRMED' WHERE customer_name = :#${body.customerName} " +
                "AND product_name = :#${body.productName} AND status = 'PROCESSING'" +
                "?dataSource=#xaDataSource")
            .log("Order confirmation sent");

        // Error handling route
        onException(Exception.class)
            .handled(true)
            .log("Transaction failed, rolling back: ${exception.message}")
            .to("sql:INSERT INTO audit_log (operation, table_name, details, transaction_id) " +
                "VALUES ('ROLLBACK', 'transaction', '${exception.message}', '${exchangeId}')" +
                "?dataSource=#xaDataSource")
            .setBody(simple("Order processing failed: ${exception.message}"))
            .to("jms:queue:errors");
    }
}
