package com.alnafi.camel.lab4.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for custom processor routes
 */
public class CustomProcessorRouteTest extends CamelTestSupport {

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                CustomProcessorRouteBuilder customRoutes = new CustomProcessorRouteBuilder();
                customRoutes.setContext(context());
                customRoutes.configure();

                // Redirect the shared output endpoint to a mock for assertions
                interceptSendToEndpoint("direct:output")
                    .skipSendToOriginalEndpoint()
                    .to("mock:result");
            }
        };
    }

    @Test
    public void testTransformRoute() throws Exception {
        MockEndpoint mockResult = getMockEndpoint("mock:result");
        mockResult.expectedMessageCount(1);

        template.sendBody("direct:transform", "test message");

        assertMockEndpointsSatisfied();

        String resultBody = mockResult.getReceivedExchanges().get(0).getIn().getBody(String.class);
        assertTrue("Message should be transformed to uppercase", resultBody.startsWith("TEST MESSAGE"));
        assertTrue("Message should contain processing timestamp", resultBody.contains("PROCESSED_AT_"));
    }

    @Test
    public void testEnrichmentRoute() throws Exception {
        MockEndpoint mockResult = getMockEndpoint("mock:result");
        mockResult.expectedMessageCount(1);

        template.sendBodyAndHeader("direct:enrich", "USER001 account inquiry",
                                 "MessageType", "USER_REQUEST");

        assertMockEndpointsSatisfied();

        String resultBody = mockResult.getReceivedExchanges().get(0).getIn().getBody(String.class);
        assertTrue("Message should be enriched with user data",
                  resultBody.contains("John Doe - Premium Customer"));
        assertTrue("Message should contain message type", resultBody.startsWith("[USER_REQUEST]"));
    }

    @Test
    public void testCombinedProcessing() throws Exception {
        MockEndpoint mockResult = getMockEndpoint("mock:result");
        mockResult.expectedMessageCount(1);

        template.sendBodyAndHeader("direct:combined", "USER002 service request",
                                 "MessageType", "USER_REQUEST");

        assertMockEndpointsSatisfied();

        Exchange resultExchange = mockResult.getReceivedExchanges().get(0);
        String resultBody = resultExchange.getIn().getBody(String.class);

        assertTrue("Message should contain enrichment data",
                  resultBody.contains("Jane Smith - Standard Customer"));
        assertEquals("Should have ProcessedBy header",
                    "MessageTransformProcessor",
                    resultExchange.getIn().getHeader("ProcessedBy"));
        assertEquals("Should have EnrichedBy header",
                    "MessageEnrichmentProcessor",
                    resultExchange.getIn().getHeader("EnrichedBy"));
    }
}
