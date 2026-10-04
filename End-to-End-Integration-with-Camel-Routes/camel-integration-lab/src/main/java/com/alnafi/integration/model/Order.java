package com.alnafi.integration.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private String orderNumber;
    private String customerCode;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status;

    private String customerName;
    private String customerEmail;

    public Order() {}

    public Order(String orderNumber, String customerCode, BigDecimal totalAmount) {
        this.orderNumber = orderNumber;
        this.customerCode = customerCode;
        this.totalAmount = totalAmount;
        this.orderDate = LocalDateTime.now();
        this.status = "PENDING";
    }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    @Override
    public String toString() {
        return "Order{orderNumber='" + orderNumber + "', customerCode='" + customerCode +
                "', totalAmount=" + totalAmount + ", status='" + status + "'}";
    }
}
