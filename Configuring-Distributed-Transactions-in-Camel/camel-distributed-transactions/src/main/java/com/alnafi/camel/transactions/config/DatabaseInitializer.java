package com.alnafi.camel.transactions.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DataSource xaDataSource;

    @Override
    public void run(String... args) throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(xaDataSource);
        
        // Read and execute schema.sql
        ClassPathResource resource = new ClassPathResource("schema.sql");
        byte[] binaryData = FileCopyUtils.copyToByteArray(resource.getInputStream());
        String sql = new String(binaryData, StandardCharsets.UTF_8);
        
        // Split by semicolon and execute each statement
        String[] statements = sql.split(";");
        for (String statement : statements) {
            if (!statement.trim().isEmpty()) {
                jdbcTemplate.execute(statement.trim());
            }
        }
        
        System.out.println("Database initialized successfully");
    }
}
