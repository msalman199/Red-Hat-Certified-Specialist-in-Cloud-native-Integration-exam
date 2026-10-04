package com.example.camel;

import org.apache.camel.builder.RouteBuilder;

public class FallbackRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("file:input/fallback?noop=true&delay=5000")
            .routeId("fallback-route")
            .log("Processing message with fallback strategy: ${body}")
            .doTry()
                .process(new FailureSimulatorProcessor(0.6))
                .log("Primary processing successful: ${body}")
                .to("file:output/fallback/primary")
            .doCatch(Exception.class)
                .log("Primary processing failed, applying fallback: ${exception.message}")
                .process(new FallbackProcessor())
                .log("Fallback processing completed: ${body}")
                .to("file:output/fallback/fallback")
            .end();

        from("file:input/fallback-alt?noop=true&delay=5000")
            .routeId("fallback-alt-route")
            .onException(Exception.class)
                .handled(true)
                .log("Exception caught, applying fallback: ${exception.message}")
                .process(new FallbackProcessor())
                .to("file:output/fallback/alternative")
            .end()
            .log("Processing message with alternative fallback: ${body}")
            .process(new FailureSimulatorProcessor(0.5))
            .to("file:output/fallback/alt-primary");
    }
}
