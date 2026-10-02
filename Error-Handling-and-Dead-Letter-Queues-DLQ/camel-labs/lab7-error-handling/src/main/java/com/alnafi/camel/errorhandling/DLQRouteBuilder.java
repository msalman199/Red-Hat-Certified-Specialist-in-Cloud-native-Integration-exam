package com.alnafi.camel.errorhandling;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.LoggingLevel;

public class DLQRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        onException(IllegalArgumentException.class)
            .log(LoggingLevel.ERROR, "Validation error: ${exception.message}")
            .maximumRedeliveries(0)
            .setHeader("ErrorCategory", constant("VALIDATION"))
            .setHeader("FailureTime", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
            .handled(true)
            .to("direct:dlq.validation");

        onException(RuntimeException.class)
            .log(LoggingLevel.WARN, "RuntimeException occurred: ${exception.message}")
            .maximumRedeliveries(3)
            .redeliveryDelay(500)
            .backOffMultiplier(2)
            .useExponentialBackOff()
            .retryAttemptedLogLevel(LoggingLevel.WARN)
            .retriesExhaustedLogLevel(LoggingLevel.ERROR)
            .setHeader("ErrorCategory", constant("RUNTIME"))
            .setHeader("FailureTime", simple("${date:now:yyyy-MM-dd HH:mm:ss}"))
            .handled(true)
            .to("direct:dlq.runtime");

        from("direct:input.messages")
            .routeId("input-message-processor")
            .log("Processing message from input queue: ${body}")
            .bean(ErrorSimulationService.class, "processMessage")
            .log("Message processed successfully: ${body}")
            .to("direct:output.success");

        from("direct:output.success")
            .routeId("success-consumer")
            .log("Success: ${body}")
            .to("file:output/success?fileName=success-${date:now:yyyyMMdd-HHmmssSSS}.txt");

        from("direct:dlq.runtime")
            .routeId("dlq-runtime-monitor")
            .log(LoggingLevel.ERROR, "DLQ Runtime Error: ${body}")
            .setBody(simple("RuntimeError at ${header.FailureTime}: ${body}"))
            .to("file:output/dlq/runtime-errors?fileName=runtime-error-${date:now:yyyyMMdd-HHmmssSSS}.txt");

        from("direct:dlq.validation")
            .routeId("dlq-validation-monitor")
            .log(LoggingLevel.ERROR, "DLQ Validation Error: ${body}")
            .setBody(simple("ValidationError at ${header.FailureTime}: ${body}"))
            .to("file:output/dlq/validation-errors?fileName=validation-error-${date:now:yyyyMMdd-HHmmssSSS}.txt");
    }
}
