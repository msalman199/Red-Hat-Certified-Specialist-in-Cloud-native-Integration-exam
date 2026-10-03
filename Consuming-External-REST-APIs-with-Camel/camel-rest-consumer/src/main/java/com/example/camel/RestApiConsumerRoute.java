package com.example.camel;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.common.HttpMethods;

public class RestApiConsumerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Global error handler for network and API failures
        onException(Exception.class)
            .handled(true)
            .log("ERROR occurred while calling external API: ${exception.message}")
            .to("direct:handleError");

        // Route 1: Fetch a small batch of posts from JSONPlaceholder
        from("timer://fetchPosts?period=20000&repeatCount=3")
            .routeId("fetch-posts-route")
            .log("Fetching posts from JSONPlaceholder API...")
            .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
            .setHeader("Accept", constant("application/json"))
            .to("https://jsonplaceholder.typicode.com/posts?_limit=5&bridgeEndpoint=true")
            .log("Posts API responded with status ${header.CamelHttpResponseCode}")
            .choice()
                .when(header("CamelHttpResponseCode").isEqualTo(200))
                    .to("direct:processPostsData")
                .otherwise()
                    .to("direct:handleApiError")
            .end();

        // Route 2: Fetch a single user's information
        from("timer://fetchUser?period=25000&repeatCount=2")
            .routeId("fetch-user-route")
            .log("Fetching user information...")
            .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
            .setHeader("Accept", constant("application/json"))
            .to("https://jsonplaceholder.typicode.com/users/1?bridgeEndpoint=true")
            .log("Users API responded with status ${header.CamelHttpResponseCode}")
            .choice()
                .when(header("CamelHttpResponseCode").isEqualTo(200))
                    .to("direct:processUserData")
                .otherwise()
                    .to("direct:handleApiError")
            .end();

        // Route 3: Process the posts array
        from("direct:processPostsData")
            .routeId("process-posts-route")
            .log("Processing posts data...")
            .split().jsonpath("$[*]")
                .log("Post received - ID: ${body[id]}, Title: ${body[title]}")
                .to("direct:savePostData")
            .end();

        // Route 4: Process the single user object
        from("direct:processUserData")
            .routeId("process-user-route")
            .log("Processing user data...")
            .unmarshal().json()
            .log("User Info - Name: ${body[name]}, Email: ${body[email]}")
            .to("direct:saveUserData");

        // Route 5: Simulate saving post data
        from("direct:savePostData")
            .routeId("save-post-route")
            .log("Saving post: ${body[title]}");

        // Route 6: Simulate saving user data
        from("direct:saveUserData")
            .routeId("save-user-route")
            .log("Saving user: ${body[name]}");

        // Route 7: Handle non-200 API responses
        from("direct:handleApiError")
            .routeId("handle-api-error-route")
            .log("API error handler triggered. Status code: ${header.CamelHttpResponseCode}");

        // Route 8: Handle exceptions (timeouts, connection errors)
        from("direct:handleError")
            .routeId("general-error-handler-route")
            .log("General error handler triggered.");
    }
}
