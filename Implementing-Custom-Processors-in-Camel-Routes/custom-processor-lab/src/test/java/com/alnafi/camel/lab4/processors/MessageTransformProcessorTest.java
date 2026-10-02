package com.alnafi.camel.lab4.processors;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for MessageTransformProcessor
 */
public class MessageTransformProcessorTest {

    private MessageTransformProcessor processor;
    private Exchange exchange;

    @Before
    public void setUp() {
        processor = new MessageTransformProcessor();
        exchange = new DefaultExchange(new DefaultCamelContext());
    }

    @Test
    public void testBasicMessageTransformation() throws Exception {
        exchange.getIn().setBody("hello world");

        processor.process(exchange);

        String transformedMessage = exchange.getIn().getBody(String.class);
        assertTrue("Message should be uppercase", transformedMessage.startsWith("HELLO WORLD"));
        assertTrue("Message should contain timestamp", transformedMessage.contains("PROCESSED_AT_"));
    }

    @Test
    public void testNullMessageHandling() throws Exception {
        exchange.getIn().setBody(null, String.class);

        processor.process(exchange);

        String transformedMessage = exchange.getIn().getBody(String.class);
        assertTrue("Null message should be handled",
                  transformedMessage.startsWith("NULL_MESSAGE_PROCESSED_AT_"));
    }

    @Test
    public void testCustomHeadersAdded() throws Exception {
        exchange.getIn().setBody("test message");

        processor.process(exchange);

        assertEquals("ProcessedBy header should be set",
                    "MessageTransformProcessor",
                    exchange.getIn().getHeader("ProcessedBy"));
        assertNotNull("ProcessingTimestamp should be set",
                     exchange.getIn().getHeader("ProcessingTimestamp"));
        assertNotNull("MessageLength should be set",
                     exchange.getIn().getHeader("MessageLength"));
    }
}
