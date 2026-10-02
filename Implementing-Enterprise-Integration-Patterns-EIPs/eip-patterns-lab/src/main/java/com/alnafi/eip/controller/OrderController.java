package com.alnafi.eip.controller;

import com.alnafi.eip.model.CustomerOrder;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private ProducerTemplate producerTemplate;
    
    @PostMapping("/process")
    public ResponseEntity<String> processOrder(@RequestBody CustomerOrder order) {
        try {
            // Convert order to JSON and send to Content-Based Router
            producerTemplate.sendBody("direct:processOrder", order);
            return ResponseEntity.ok("Order " + order.getOrderId() + " processed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing order: " + e.getMessage());
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Order processing service is running!");
    }
}
