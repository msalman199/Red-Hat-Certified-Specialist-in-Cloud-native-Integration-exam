package com.example.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.LoggingLevel;

public class RetryRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        errorHandler(defaultErrorHandler()
            .maximumRedeliveries(3)
            .redeliveryDelay(2000)
            .retryAttemptedLogLevel(LoggingLevel.WARN)
            .retriesExhaustedLogLevel(LoggingLevel.ERROR)
            .logRetryAttempted(true)
            .logExhausted(true));

        from("file:input/retry?noop=true&delay=5000")
            .routeId("retry-route")
            .log("Processing message with retry strategy: ${body}")
            .process(new FailureSimulatorProcessor(0.7))
            .log("Message processed successfully: ${body}")
            .to("file:output/retry");
    }
}
