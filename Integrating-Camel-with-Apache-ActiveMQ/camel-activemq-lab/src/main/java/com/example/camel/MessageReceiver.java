package com.example.camel;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Scanner;

/**
 * Utility class for receiving messages from ActiveMQ queues.
 */
public class MessageReceiver {

    private static final Logger logger = LoggerFactory.getLogger(MessageReceiver.class);
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final int RECEIVE_TIMEOUT_MS = 5000;

    public static void main(String[] args) throws Exception {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Message Receiver Menu ===");
            System.out.println("1. Receive from output.queue");
            System.out.println("2. Receive from fulfillment.queue");
            System.out.println("3. Receive from review.queue");
            System.out.println("4. Receive from success.queue");
            System.out.println("5. Receive from dead.letter.queue");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");

            int choice = Integer.parseInt(scanner.nextLine().trim());

            if (choice == 6) {
                break;
            }

            String queueName = queueForChoice(choice);
            if (queueName == null) {
                System.out.println("Invalid choice!");
                continue;
            }

            receiveMessage(session, queueName);
        }

        session.close();
        connection.close();
        scanner.close();
    }

    private static String queueForChoice(int choice) {
        switch (choice) {
            case 1: return "output.queue";
            case 2: return "fulfillment.queue";
            case 3: return "review.queue";
            case 4: return "success.queue";
            case 5: return "dead.letter.queue";
            default: return null;
        }
    }

    private static void receiveMessage(Session session, String queueName) {
        try {
            Destination destination = session.createQueue(queueName);
            MessageConsumer consumer = session.createConsumer(destination);

            System.out.println("Waiting up to " + (RECEIVE_TIMEOUT_MS / 1000) + " seconds for a message on " + queueName + "...");
            Message message = consumer.receive(RECEIVE_TIMEOUT_MS);

            if (message == null) {
                System.out.println("No message received from " + queueName + " within timeout.");
            } else if (message instanceof TextMessage) {
                String text = ((TextMessage) message).getText();
                logger.info("Received message from {}: {}", queueName, text);
                System.out.println("Received: " + text);
            } else {
                System.out.println("Received a non-text message from " + queueName);
            }

            consumer.close();
        } catch (Exception e) {
            logger.error("Error receiving message from " + queueName, e);
        }
    }
}
