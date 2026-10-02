package com.alnafi.camel.lab4;

import com.alnafi.camel.lab4.routes.CustomProcessorRouteBuilder;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for custom processor demonstration
 */
public class CustomProcessorApplication {

    private static final Logger logger = LoggerFactory.getLogger(CustomProcessorApplication.class);

    public static void main(String[] args) throws Exception {
        logger.info("Starting Custom Processor Lab Application");

        CamelContext camelContext = new DefaultCamelContext();

        try {
            camelContext.addRoutes(new CustomProcessorRouteBuilder());
            camelContext.start();
            logger.info("Camel context started successfully");

            ProducerTemplate producer = camelContext.createProducerTemplate();

            testRoutes(producer);

            logger.info("Application running. Timer route will fire 3 times over 15 seconds.");
            Thread.sleep(20000);

        } finally {
            camelContext.stop();
            logger.info("Camel context stopped");
        }
    }

    private static void testRoutes(ProducerTemplate producer) throws Exception {
        logger.info("=== Starting Route Testing ===");

        logger.info("--- Test 1: Basic Transformation ---");
        producer.sendBody("direct:transform", "hello world from camel");
        Thread.sleep(500);

        logger.info("--- Test 2: User Message Enrichment ---");
        producer.sendBodyAndHeader("direct:enrich", "USER001 requesting account balance",
                                 "MessageType", "USER_REQUEST");
        Thread.sleep(500);

        logger.info("--- Test 3: Unknown User Message ---");
        producer.sendBodyAndHeader("direct:enrich", "USER999 unknown user request",
                                 "MessageType", "USER_REQUEST");
        Thread.sleep(500);

        logger.info("--- Test 4: Combined Processing ---");
        producer.sendBodyAndHeader("direct:combined", "USER002 premium service request",
                                 "MessageType", "USER_REQUEST");
        Thread.sleep(500);

        logger.info("--- Test 5: System Message ---");
        producer.sendBodyAndHeader("direct:enrich", "System maintenance scheduled",
                                 "MessageType", "SYSTEM_MESSAGE");
        Thread.sleep(500);

        logger.info("=== Route Testing Completed ===");
    }
}
