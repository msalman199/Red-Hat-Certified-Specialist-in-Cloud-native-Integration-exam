package com.example.camel;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

public class UserRestRoute extends RouteBuilder {
    
    private UserService userService = new UserService();

    @Override
    public void configure() throws Exception {
        
        // Configure REST configuration
        restConfiguration()
            .component("jetty")
            .host("0.0.0.0")
            .port(8080)
            .bindingMode(RestBindingMode.json)
            .dataFormatProperty("prettyPrint", "true")
            .enableCORS(true)
            .corsHeaderProperty("Access-Control-Allow-Origin", "*")
            .corsHeaderProperty("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            .corsHeaderProperty("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Exception handling
        onException(Exception.class)
            .handled(true)
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .setBody(constant("{\"error\": \"Internal server error\", \"message\": \"${exception.message}\"}"));

        // REST API definition
        rest("/api/users")
            .description("User REST service")
            .consumes("application/json")
            .produces("application/json")

            // GET /api/users - Get all users
            .get()
                .description("Get all users")
                .outType(User[].class)
                .responseMessage().code(200).message("Users retrieved successfully").endResponseMessage()
                .to("direct:getAllUsers")

            // GET /api/users/{id} - Get user by ID
            .get("/{id}")
                .description("Get user by ID")
                .param().name("id").type(path).description("User ID").dataType("long").endParam()
                .outType(User.class)
                .responseMessage().code(200).message("User found").endResponseMessage()
                .responseMessage().code(404).message("User not found").endResponseMessage()
                .to("direct:getUserById")

            // POST /api/users - Create new user
            .post()
                .description("Create a new user")
                .type(User.class)
                .outType(User.class)
                .responseMessage().code(201).message("User created successfully").endResponseMessage()
                .responseMessage().code(400).message("Invalid user data").endResponseMessage()
                .to("direct:createUser")

            // PUT /api/users/{id} - Update user
            .put("/{id}")
                .description("Update an existing user")
                .param().name("id").type(path).description("User ID").dataType("long").endParam()
                .type(User.class)
                .outType(User.class)
                .responseMessage().code(200).message("User updated successfully").endResponseMessage()
                .responseMessage().code(404).message("User not found").endResponseMessage()
                .to("direct:updateUser")

            // DELETE /api/users/{id} - Delete user
            .delete("/{id}")
                .description("Delete a user")
                .param().name("id").type(path).description("User ID").dataType("long").endParam()
                .responseMessage().code(204).message("User deleted successfully").endResponseMessage()
                .responseMessage().code(404).message("User not found").endResponseMessage()
                .to("direct:deleteUser");

        // Route implementations
        
        // Get all users route
        from("direct:getAllUsers")
            .log("Getting all users")
            .process(exchange -> {
                exchange.getIn().setBody(userService.getAllUsers());
            });

        // Get user by ID route
        from("direct:getUserById")
            .log("Getting user by ID: ${header.id}")
            .process(exchange -> {
                Long id = Long.valueOf(exchange.getIn().getHeader("id", String.class));
                User user = userService.getUserById(id);
                if (user != null) {
                    exchange.getIn().setBody(user);
                } else {
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
                    exchange.getIn().setBody("{\"error\": \"User not found\", \"id\": " + id + "}");
                }
            });

        // Create user route
        from("direct:createUser")
            .log("Creating new user: ${body}")
            .process(exchange -> {
                User user = exchange.getIn().getBody(User.class);
                if (user != null && user.getName() != null && user.getEmail() != null) {
                    User createdUser = userService.createUser(user);
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 201);
                    exchange.getIn().setBody(createdUser);
                } else {
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
                    exchange.getIn().setBody("{\"error\": \"Invalid user data\", \"message\": \"Name and email are required\"}");
                }
            });

        // Update user route
        from("direct:updateUser")
            .log("Updating user ID: ${header.id}")
            .process(exchange -> {
                Long id = Long.valueOf(exchange.getIn().getHeader("id", String.class));
                User user = exchange.getIn().getBody(User.class);
                if (user != null && user.getName() != null && user.getEmail() != null) {
                    User updatedUser = userService.updateUser(id, user);
                    if (updatedUser != null) {
                        exchange.getIn().setBody(updatedUser);
                    } else {
                        exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
                        exchange.getIn().setBody("{\"error\": \"User not found\", \"id\": " + id + "}");
                    }
                } else {
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
                    exchange.getIn().setBody("{\"error\": \"Invalid user data\", \"message\": \"Name and email are required\"}");
                }
            });

        // Delete user route
        from("direct:deleteUser")
            .log("Deleting user ID: ${header.id}")
            .process(exchange -> {
                Long id = Long.valueOf(exchange.getIn().getHeader("id", String.class));
                boolean deleted = userService.deleteUser(id);
                if (deleted) {
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 204);
                    exchange.getIn().setBody("");
                } else {
                    exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 404);
                    exchange.getIn().setBody("{\"error\": \"User not found\", \"id\": " + id + "}");
                }
            });
    }
}
