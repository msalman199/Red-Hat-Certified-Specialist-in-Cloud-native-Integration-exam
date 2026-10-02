package com.alnafi.camel.xmldsl;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@CamelSpringBootTest
@SpringBootTest(classes = CamelXmlDslApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class XmlDslRouteTest {

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Test
    public void testCamelContextStartup() {
        assertNotNull(camelContext);
        assertTrue(camelContext.getStatus().isStarted());
    }

    @Test
    public void testRouteDefinitions() {
        // Verify that our XML routes are loaded
        assertTrue(camelContext.getRoutes().size() > 0);
        
        // Check for specific route IDs
        boolean fileProcessingRouteExists = camelContext.getRoutes().stream()
            .anyMatch(route -> "fileProcessingRoute".equals(route.getId()));
        assertTrue(fileProcessingRouteExists, "File processing route should be loaded");
        
        boolean contentBasedRouterExists = camelContext.getRoutes().stream()
            .anyMatch(route -> "contentBasedRouter".equals(route.getId()));
        assertTrue(contentBasedRouterExists, "Content-based router should be loaded");
    }

    @Test
    public void testDirectRouteProcessing() throws Exception {
        // Test premium processing route
        String testOrder = "{\"orderId\": \"12345\", \"customerType\": \"1\", \"amount\": 500}";
        
        String result = producerTemplate.requestBody("direct:premiumProcessing", testOrder, String.class);
        
        assertNotNull(result);
        assertTrue(result.contains("PREMIUM"));
        assertTrue(result.contains("0.15")); // Premium discount
    }
}
