package com.alnafi.camel.errorhandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorSimulationService {
    private static final Logger logger = LoggerFactory.getLogger(ErrorSimulationService.class);
    private int callCount = 0;

    public String processMessage(String message) throws Exception {
        callCount++;
        logger.info("Processing message attempt #{}: {}", callCount, message);

        if (message.contains("NETWORK_ERROR")) {
            throw new RuntimeException("Network connection failed");
        } else if (message.contains("VALIDATION_ERROR")) {
            throw new IllegalArgumentException("Invalid message format");
        } else if (message.contains("RETRY_SUCCESS") && callCount < 3) {
            throw new RuntimeException("Temporary failure - attempt " + callCount);
        }

        logger.info("Message processed successfully: {}", message);
        return "Processed: " + message;
    }

    public void resetCallCount() {
        callCount = 0;
    }
}
