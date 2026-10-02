package com.alnafi.camel.errorhandling;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.LoggingLevel;

public class ErrorHandlingRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        onException(IllegalArgumentException.class)
            .log(LoggingLevel.ERROR, "Validation error - no retry: ${exception.message}")
            .maximumRedeliveries(0)
            .handled(true)
            .to("direct:validation-error-handler");

        onException(RuntimeException.class)
            .log(LoggingLevel.WARN, "RuntimeException occurred: ${exception.message}")
            .maximumRedeliveries(3)
            .redeliveryDelay(1000)
            .backOffMultiplier(2)
            .useExponentialBackOff()
            .retryAttemptedLogLevel(LoggingLevel.WARN)
            .retriesExhaustedLogLevel(LoggingLevel.ERROR)
            .handled(true)
            .to("direct:failure-handler");

        from("direct:process-message")
            .routeId("main-processing-route")
            .log("Received message: ${body}")
            .bean(ErrorSimulationService.class, "processMessage")
            .log("Successfully processed: ${body}")
            .to("direct:success-handler");

        from("direct:success-handler")
            .routeId("success-handler")
            .log("Message processing completed successfully: ${body}")
            .to("file:output/success?fileName=success-${date:now:yyyyMMdd-HHmmssSSS}.txt");

        from("direct:validation-error-handler")
            .routeId("validation-error-handler")
            .log("Handling validation error for message: ${body}")
            .setBody(simple("Validation failed for: ${body}"))
            .to("file:output/validation-errors?fileName=validation-error-${date:now:yyyyMMdd-HHmmssSSS}.txt");

        from("direct:failure-handler")
            .routeId("failure-handler")
            .log("Handling exhausted-retry failure for message: ${body}")
            .setBody(simple("Failed after retries: ${body}"))
            .to("file:output/failures?fileName=failure-${date:now:yyyyMMdd-HHmmssSSS}.txt");
    }
}
