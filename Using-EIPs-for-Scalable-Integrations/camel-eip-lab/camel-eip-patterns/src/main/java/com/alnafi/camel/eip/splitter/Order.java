package com.alnafi.camel.eip.splitter;

import java.util.List;

public class Order {
    private String orderId;
    private String customerId;
    private List<OrderItem> items;

    public Order() {}

    public Order(String orderId, String customerId, List<OrderItem> items) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    @Override
    public String toString() {
        return "Order{orderId='" + orderId + "', customerId='" + customerId +
               "', items=" + items + "}";
    }
}
