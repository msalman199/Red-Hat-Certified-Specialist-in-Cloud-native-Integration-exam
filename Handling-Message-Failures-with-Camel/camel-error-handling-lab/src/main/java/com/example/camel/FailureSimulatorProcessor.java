package com.example.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class FailureSimulatorProcessor implements Processor {

    private final double failureRate;
    private final Random random = new Random();
    private final AtomicInteger processCount = new AtomicInteger(0);

    public FailureSimulatorProcessor(double failureRate) {
        this.failureRate = failureRate;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        int count = processCount.incrementAndGet();
        String body = exchange.getIn().getBody(String.class);

        if (random.nextDouble() < failureRate) {
            if (count % 3 == 0) {
                throw new RuntimeException("Simulated runtime exception for message: " + body);
            } else if (count % 5 == 0) {
                throw new IllegalArgumentException("Simulated validation error for message: " + body);
            } else {
                throw new Exception("Simulated general exception for message: " + body);
            }
        }

        exchange.getIn().setBody("Processed successfully: " + body + " (attempt #" + count + ")");
        System.out.println("Successfully processed: " + body);
    }
}
