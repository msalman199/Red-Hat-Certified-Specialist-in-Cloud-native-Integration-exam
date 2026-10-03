package com.alnafi.camel.transactions.controller;

import com.alnafi.camel.transactions.service.TestDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private TestDataService testDataService;

    @Autowired
    private DataSource xaDataSource;

    @PostMapping("/send-valid-order")
    public ResponseEntity<String> sendValidOrder() {
        try {
            testDataService.sendValidOrder();
            return ResponseEntity.ok("Valid order sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending order: " + e.getMessage());
        }
    }

    @PostMapping("/send-invalid-order")
    public ResponseEntity<String> sendInvalidOrder() {
        try {
            testDataService.sendInvalidOrder();
            return ResponseEntity.ok("Invalid order sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending order: " + e.getMessage());
        }
    }

    @PostMapping("/send-insufficient-inventory-order")
    public ResponseEntity<String> sendInsufficientInventoryOrder() {
        try {
            testDataService.sendInsufficientInventoryOrder();
            return ResponseEntity.ok("Insufficient inventory order sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending order: " + e.getMessage());
        }
    }

    @PostMapping("/send-invalid-data-order")
    public ResponseEntity<String> sendInvalidDataOrder() {
        try {
            testDataService.sendInvalidDataOrder();
            return ResponseEntity.ok("Invalid data order sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending order: " + e.getMessage());
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Map<String, Object>>> getOrders() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(xaDataSource);
        List<Map<String, Object>> orders = jdbcTemplate.queryForList("SELECT * FROM orders ORDER BY created_at DESC");
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/inventory")
    public ResponseEntity<List<Map<String, Object>>> getInventory() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(xaDataSource);
        List<Map<String, Object>> inventory = jdbcTemplate.queryForList("SELECT * FROM inventory ORDER BY product_name");
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/audit-log")
    public ResponseEntity<List<Map<String, Object>>> getAuditLog() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(xaDataSource);
        List<Map<String, Object>> auditLog = jdbcTemplate.queryForList("SELECT * FROM audit_log ORDER BY created_at DESC LIMIT 50");
        return ResponseEntity.ok(auditLog);
    }

    @PostMapping("/reset-data")
    public ResponseEntity<String> resetData() {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(xaDataSource);
            
            // Clear existing data
            jdbcTemplate.execute("DELETE FROM orders");
            jdbcTemplate.execute("DELETE FROM audit_log");
            jdbcTemplate.execute("UPDATE inventory SET available_quantity = CASE " +
                "WHEN product_name = 'Laptop' THEN 10 " +
                "WHEN product_name = 'Mouse' THEN 50 " +
                "WHEN product_name = 'Keyboard' THEN 25 " +
                "WHEN product_name = 'Monitor' THEN 8 " +
                "ELSE available_quantity END, reserved_quantity = 0");
            
            return ResponseEntity.ok("Test data reset successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error resetting data: " + e.getMessage());
        }
    }
}
