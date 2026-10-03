package com.example.camel;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Scanner;

/**
 * Utility class for sending test messages to ActiveMQ queues.
 */
public class MessageSender {

    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);
    private static final String BROKER_URL = "tcp://localhost:61616";

    public static void main(String[] args) throws Exception {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Message Sender Menu ===");
            System.out.println("1. Send to input.queue (simple transformation)");
            System.out.println("2. Send to order.queue (content-based routing)");
            System.out.println("3. Send to risky.queue (error handling / dead letter)");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            int choice = Integer.parseInt(scanner.nextLine().trim());

            if (choice == 4) {
                break;
            }

            String queueName = queueForChoice(choice);
            if (queueName == null) {
                System.out.println("Invalid choice!");
                continue;
            }

            System.out.print("Enter message content: ");
            String messageContent = scanner.nextLine();

            Destination destination = session.createQueue(queueName);
            MessageProducer producer = session.createProducer(destination);
            TextMessage message = session.createTextMessage(messageContent);
            producer.send(message);
            producer.close();

            logger.info("Message sent to {}: {}", queueName, messageContent);
            System.out.println("Message sent successfully to " + queueName);
        }

        session.close();
        connection.close();
        scanner.close();
    }

    private static String queueForChoice(int choice) {
        switch (choice) {
            case 1: return "input.queue";
            case 2: return "order.queue";
            case 3: return "risky.queue";
            default: return null;
        }
    }
}
