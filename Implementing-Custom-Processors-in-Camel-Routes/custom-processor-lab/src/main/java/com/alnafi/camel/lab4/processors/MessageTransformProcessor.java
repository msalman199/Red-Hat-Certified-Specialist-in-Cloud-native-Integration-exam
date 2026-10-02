package com.alnafi.camel.lab4.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom processor that transforms incoming messages by:
 * 1. Converting text to uppercase
 * 2. Adding a timestamp
 * 3. Adding custom headers
 */
public class MessageTransformProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(MessageTransformProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        logger.info("Processing message in custom processor");

        String originalMessage = exchange.getIn().getBody(String.class);
        logger.info("Original message: {}", originalMessage);

        String transformedMessage = transformMessage(originalMessage);
        exchange.getIn().setBody(transformedMessage);

        addCustomHeaders(exchange, transformedMessage);

        logger.info("Transformed message: {}", transformedMessage);
    }

    private String transformMessage(String originalMessage) {
        if (originalMessage == null) {
            return "NULL_MESSAGE_PROCESSED_AT_" + System.currentTimeMillis();
        }
        return originalMessage.toUpperCase() + " [PROCESSED_AT_" + System.currentTimeMillis() + "]";
    }

    private void addCustomHeaders(Exchange exchange, String transformedMessage) {
        exchange.getIn().setHeader("ProcessedBy", "MessageTransformProcessor");
        exchange.getIn().setHeader("ProcessingTimestamp", System.currentTimeMillis());
        exchange.getIn().setHeader("MessageLength", transformedMessage.length());
    }
}
