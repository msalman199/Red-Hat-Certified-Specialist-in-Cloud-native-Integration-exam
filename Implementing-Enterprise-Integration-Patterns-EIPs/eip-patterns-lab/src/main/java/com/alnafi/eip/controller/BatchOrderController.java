package com.alnafi.eip.controller;

import com.alnafi.eip.model.BatchOrder;
import com.alnafi.eip.model.CustomerOrder;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/batch")
public class BatchOrderController {
    
    @Autowired
    private ProducerTemplate producerTemplate;
    
    @PostMapping("/process")
    public ResponseEntity<String> processBatchOrder(@RequestBody BatchOrder batchOrder) {
        try {
            producerTemplate.sendBody("direct:processBatchOrder", batchOrder);
            return ResponseEntity.ok("Batch " + batchOrder.getBatchId() + 
                                   " with " + batchOrder.getTotalOrders() + 
                                   " orders processed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing batch: " + e.getMessage());
        }
    }
    
    @PostMapping("/process-with-aggregation")
    public ResponseEntity<String> processBatchWithAggregation(@RequestBody BatchOrder batchOrder) {
        try {
            producerTemplate.sendBody("direct:processBatchWithAggregation", batchOrder);
            return ResponseEntity.ok("Batch " + batchOrder.getBatchId() + 
                                   " processed with aggregation");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing batch: " + e.getMessage());
        }
    }
    
    @GetMapping("/sample")
    public ResponseEntity<BatchOrder> getSampleBatch() {
        // Create sample orders for testing
        List<CustomerOrder> sampleOrders = Arrays.asList(
            new CustomerOrder("ORD-001", "PREMIUM", 1500.0, "HIGH", "ELECTRONICS"),
            new CustomerOrder("ORD-002", "STANDARD", 250.0, "NORMAL", "CLOTHING"),
            new CustomerOrder("ORD-003", "PREMIUM", 800.0, "NORMAL", "BOOKS"),
            new CustomerOrder("ORD-004", "STANDARD", 150.0, "HIGH", "ELECTRONICS"),
            new CustomerOrder("ORD-005", "PREMIUM", 2000.0, "HIGH", "FURNITURE")
        );
        
        BatchOrder sampleBatch = new BatchOrder("BATCH-001", sampleOrders);
        return ResponseEntity.ok(sampleBatch);
    }
}
