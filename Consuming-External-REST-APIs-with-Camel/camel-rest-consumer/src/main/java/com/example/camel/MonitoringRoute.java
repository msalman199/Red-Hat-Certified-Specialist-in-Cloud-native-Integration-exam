package com.example.camel;

import org.apache.camel.builder.RouteBuilder;

public class MonitoringRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("timer://monitor?period=30000&repeatCount=2")
            .routeId("monitoring-route")
            .log("=== API MONITORING CHECK ===")
            .to("https://jsonplaceholder.typicode.com/posts/1?bridgeEndpoint=true")
            .choice()
                .when(header("CamelHttpResponseCode").isEqualTo(200))
                    .log("API Health Check: HEALTHY (status ${header.CamelHttpResponseCode})")
                .otherwise()
                    .log("API Health Check: UNHEALTHY (status ${header.CamelHttpResponseCode})")
            .end();
    }
}
