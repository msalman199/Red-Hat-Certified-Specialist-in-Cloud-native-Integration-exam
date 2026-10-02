package com.alnafi.eip.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class BatchOrder {
    @JsonProperty("batchId")
    private String batchId;
    
    @JsonProperty("orders")
    private List<CustomerOrder> orders;
    
    @JsonProperty("totalOrders")
    private int totalOrders;
    
    // Default constructor
    public BatchOrder() {}
    
    // Constructor with parameters
    public BatchOrder(String batchId, List<CustomerOrder> orders) {
        this.batchId = batchId;
        this.orders = orders;
        this.totalOrders = orders != null ? orders.size() : 0;
    }
    
    // Getters and Setters
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    
    public List<CustomerOrder> getOrders() { return orders; }
    public void setOrders(List<CustomerOrder> orders) { 
        this.orders = orders;
        this.totalOrders = orders != null ? orders.size() : 0;
    }
    
    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
    
    @Override
    public String toString() {
        return "BatchOrder{" +
                "batchId='" + batchId + '\'' +
                ", totalOrders=" + totalOrders +
                ", orders=" + orders +
                '}';
    }
}
