package com.alnafi.camel.transactions.service;

import com.alnafi.camel.transactions.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TestDataService {

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendValidOrder() throws Exception {
        Order order = new Order("John Doe", "Laptop", 2, new BigDecimal("999.99"));
        String orderJson = objectMapper.writeValueAsString(order);
        producerTemplate.sendBody("jms:queue:orders", orderJson);
    }

    public void sendInvalidOrder() throws Exception {
        Order order = new Order("Jane Smith", "NonExistentProduct", 1, new BigDecimal("100.00"));
        String orderJson = objectMapper.writeValueAsString(order);
        producerTemplate.sendBody("jms:queue:orders", orderJson);
    }

    public void sendInsufficientInventoryOrder() throws Exception {
        Order order = new Order("Bob Johnson", "Monitor", 20, new BigDecimal("299.99"));
        String orderJson = objectMapper.writeValueAsString(order);
        producerTemplate.sendBody("jms:queue:orders", orderJson);
    }

    public void sendInvalidDataOrder() throws Exception {
        Order order = new Order("", "Laptop", -1, new BigDecimal("-50.00"));
        String orderJson = objectMapper.writeValueAsString(order);
        producerTemplate.sendBody("jms:queue:orders", orderJson);
    }
}
