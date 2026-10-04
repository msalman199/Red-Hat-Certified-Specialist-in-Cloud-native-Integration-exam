// File: src/main/java/com/example/routing/PerformanceTest.java
package com.example.routing;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

public class PerformanceTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger errorCount = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        System.out.println("Starting performance test for dynamic routing...");

        ExecutorService executor = Executors.newFixedThreadPool(10);
        int totalRequests = 50;

        for (int i = 0; i < totalRequests; i++) {
            final int requestId = i;
            executor.submit(() -> {
                try {
                    testProductRouting(requestId);
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Error in request " + requestId + ": " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        System.out.println("Performance test completed.");
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + errorCount.get());
    }

    private static void testProductRouting(int id) throws Exception {
        String[] categories = {"ELECTRONICS", "CLOTHING", "BOOKS", "FOOD"};
        String category = categories[id % 4];

        String json = String.format(
            "{\"productId\": \"PROD%03d\", \"category\": \"%s\", \"name\": \"Product %d\"}",
            id, category, id
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/products"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            successCount.incrementAndGet();
        } else {
            errorCount.incrementAndGet();
        }
    }
}
