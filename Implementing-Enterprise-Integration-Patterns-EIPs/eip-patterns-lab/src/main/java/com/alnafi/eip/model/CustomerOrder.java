package com.alnafi.eip.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerOrder {
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("customerType")
    private String customerType;
    
    @JsonProperty("amount")
    private double amount;
    
    @JsonProperty("priority")
    private String priority;
    
    @JsonProperty("productCategory")
    private String productCategory;
    
    // Default constructor
    public CustomerOrder() {}
    
    // Constructor with parameters
    public CustomerOrder(String orderId, String customerType, double amount, 
                        String priority, String productCategory) {
        this.orderId = orderId;
        this.customerType = customerType;
        this.amount = amount;
        this.priority = priority;
        this.productCategory = productCategory;
    }
    
    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }
    
    @Override
    public String toString() {
        return "CustomerOrder{" +
                "orderId='" + orderId + '\'' +
                ", customerType='" + customerType + '\'' +
                ", amount=" + amount +
                ", priority='" + priority + '\'' +
                ", productCategory='" + productCategory + '\'' +
                '}';
    }
}
