package com.alnafi.camel.lab4.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Advanced custom processor that enriches messages with additional data
 * and handles different message formats
 */
public class MessageEnrichmentProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(MessageEnrichmentProcessor.class);

    private static final Map<String, String> enrichmentData = new HashMap<>();

    static {
        enrichmentData.put("USER001", "John Doe - Premium Customer");
        enrichmentData.put("USER002", "Jane Smith - Standard Customer");
        enrichmentData.put("USER003", "Bob Johnson - VIP Customer");
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        logger.info("Starting message enrichment process");

        String messageBody = exchange.getIn().getBody(String.class);
        String messageType = exchange.getIn().getHeader("MessageType", String.class);

        logger.info("Processing message type: {}", messageType);
        logger.info("Original message body: {}", messageBody);

        String enrichedMessage = enrichMessage(messageBody, messageType);
        exchange.getIn().setBody(enrichedMessage);

        addProcessingMetadata(exchange, messageType);

        logger.info("Message enrichment completed");
    }

    private String enrichMessage(String originalMessage, String messageType) {
        StringBuilder enriched = new StringBuilder();
        enriched.append("[").append(messageType != null ? messageType : "UNKNOWN").append("] ");

        if (originalMessage != null && originalMessage.startsWith("USER")) {
            String userId = originalMessage.split(" ")[0];
            String userData = enrichmentData.get(userId);

            if (userData != null) {
                enriched.append("ENRICHED: ").append(userData);
                enriched.append(" | Original: ").append(originalMessage);
            } else {
                enriched.append("UNKNOWN_USER: ").append(originalMessage);
            }
        } else {
            enriched.append("STANDARD: ").append(originalMessage);
        }

        enriched.append(" | Enriched at: ").append(System.currentTimeMillis());

        return enriched.toString();
    }

    private void addProcessingMetadata(Exchange exchange, String messageType) {
        exchange.getIn().setHeader("EnrichedBy", "MessageEnrichmentProcessor");
        exchange.getIn().setHeader("EnrichmentTimestamp", System.currentTimeMillis());
        exchange.getIn().setHeader("OriginalMessageType", messageType);
        exchange.getIn().setHeader("EnrichmentApplied", true);

        if ("USER_REQUEST".equals(messageType)) {
            exchange.getIn().setHeader("Priority", "HIGH");
        } else if ("SYSTEM_MESSAGE".equals(messageType)) {
            exchange.getIn().setHeader("Priority", "MEDIUM");
        } else {
            exchange.getIn().setHeader("Priority", "LOW");
        }
    }
}
