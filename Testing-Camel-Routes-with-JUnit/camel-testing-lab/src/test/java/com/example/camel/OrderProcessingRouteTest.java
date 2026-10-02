package com.example.camel;

import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderProcessingRouteTest extends CamelTestSupport {

    @EndpointInject("mock:highPriorityQueue")
    private MockEndpoint highPriorityMock;

    @EndpointInject("mock:mediumPriorityQueue")
    private MockEndpoint mediumPriorityMock;

    @EndpointInject("mock:lowPriorityQueue")
    private MockEndpoint lowPriorityMock;

    @EndpointInject("mock:orderTransformed")
    private MockEndpoint orderTransformedMock;

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new OrderProcessingRoute();
    }

    @Override
    protected String isMockEndpoints() {
        // Replace these direct endpoints with mock endpoints at test time
        return "direct:highPriorityQueue|direct:mediumPriorityQueue|direct:lowPriorityQueue|direct:orderTransformed";
    }

    @BeforeEach
    void resetMocks() {
        highPriorityMock.reset();
        mediumPriorityMock.reset();
        lowPriorityMock.reset();
        orderTransformedMock.reset();
    }

    private String createOrder(String priority, String customerId, String amount) {
        return String.format(
            "<order>" +
                "<orderId>12345</orderId>" +
                "<customerId>%s</customerId>" +
                "<priority>%s</priority>" +
                "<amount>%s</amount>" +
                "<product>Laptop</product>" +
            "</order>",
            customerId, priority, amount
        );
    }

    @Test
    @DisplayName("Routes are created and started")
    void testRouteCreation() throws Exception {
        assertEquals(3, context.getRoutes().size());
        assertTrue(context.getRouteController().getRouteStatus("order-processing-route").isStarted());
    }

    @Test
    @DisplayName("High priority order is routed to the high priority queue")
    void testHighPriorityOrderRouting() throws Exception {
        String order = createOrder("HIGH", "CUST001", "1500.00");

        highPriorityMock.expectedMessageCount(1);
        highPriorityMock.expectedBodiesReceived(order);
        mediumPriorityMock.expectedMessageCount(0);
        lowPriorityMock.expectedMessageCount(0);

        template.sendBody("direct:processOrder", order);

        assertMockEndpointsSatisfied();
    }

    @Test
    @DisplayName("Medium priority order is routed to the medium priority queue")
    void testMediumPriorityOrderRouting() throws Exception {
        String order = createOrder("MEDIUM", "CUST002", "750.00");

        highPriorityMock.expectedMessageCount(0);
        mediumPriorityMock.expectedMessageCount(1);
        mediumPriorityMock.expectedBodiesReceived(order);
        lowPriorityMock.expectedMessageCount(0);

        template.sendBody("direct:processOrder", order);

        assertMockEndpointsSatisfied();
    }

    @Test
    @DisplayName("Order without explicit HIGH/MEDIUM priority falls to the low priority queue")
    void testLowPriorityOrderRouting() throws Exception {
        String order = createOrder("LOW", "CUST003", "250.00");

        highPriorityMock.expectedMessageCount(0);
        mediumPriorityMock.expectedMessageCount(0);
        lowPriorityMock.expectedMessageCount(1);
        lowPriorityMock.expectedBodiesReceived(order);

        template.sendBody("direct:processOrder", order);

        assertMockEndpointsSatisfied();
    }

    @Test
    @DisplayName("Valid order passes validation and sets ValidationStatus header")
    void testValidOrderValidation() throws Exception {
        String order = createOrder("HIGH", "CUST001", "1500.00");

        template.sendBody("direct:validateOrder", order);

        // No exception should have been thrown; re-send through the header check
        var result = template.request("direct:validateOrder", exchange -> exchange.getIn().setBody(order));
        assertEquals("VALID", result.getIn().getHeader("ValidationStatus"));
    }

    @Test
    @DisplayName("Order with empty customer ID throws a validation exception")
    void testInvalidOrderEmptyCustomerId() {
        String order = createOrder("HIGH", "", "1000.00");

        Exception ex = assertThrows(Exception.class, () ->
            template.sendBody("direct:validateOrder", order));

        assertTrue(ex.getCause().getMessage().contains("Customer ID is required"));
    }

    @Test
    @DisplayName("Order with negative amount throws a validation exception")
    void testInvalidOrderNegativeAmount() {
        String order = createOrder("MEDIUM", "CUST004", "-100.00");

        Exception ex = assertThrows(Exception.class, () ->
            template.sendBody("direct:validateOrder", order));

        assertTrue(ex.getCause().getMessage().contains("Order amount must be positive"));
    }

    @Test
    @DisplayName("Transformation route extracts customer id as the new body")
    void testOrderTransformation() throws Exception {
        String order = createOrder("HIGH", "CUST001", "1500.00");

        orderTransformedMock.expectedMessageCount(1);
        orderTransformedMock.expectedBodiesReceived("CUST001");
        orderTransformedMock.expectedHeaderReceived("CustomerId", "CUST001");

        template.sendBody("direct:transformOrder", order);

        assertMockEndpointsSatisfied();
        assertTrue(orderTransformedMock.getExchanges().get(0).getIn().getHeader("ProcessedTimestamp") != null);
    }
}
