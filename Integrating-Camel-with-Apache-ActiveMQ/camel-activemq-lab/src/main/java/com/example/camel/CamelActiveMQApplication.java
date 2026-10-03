package com.example.camel;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.main.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class bootstrapping the Camel context with a JMS component
 * backed by ActiveMQ.
 */
public class CamelActiveMQApplication {

    private static final Logger logger = LoggerFactory.getLogger(CamelActiveMQApplication.class);
    private static final String BROKER_URL = "tcp://localhost:61616";

    public static void main(String[] args) throws Exception {
        logger.info("Starting Camel-ActiveMQ Integration Application");

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
        connectionFactory.setTrustAllPackages(true);

        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
        pooledConnectionFactory.setConnectionFactory(connectionFactory);
        pooledConnectionFactory.setMaxConnections(10);

        JmsComponent jmsComponent = JmsComponent.jmsComponentAutoAcknowledge(pooledConnectionFactory);
        jmsComponent.setConcurrentConsumers(3);

        Main main = new Main();
        main.bind("jms", jmsComponent);
        main.configure().addRoutesBuilder(new CamelRouteBuilder());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down Camel-ActiveMQ Integration Application");
            try {
                main.stop();
                pooledConnectionFactory.stop();
            } catch (Exception e) {
                logger.error("Error during shutdown", e);
            }
        }));

        logger.info("Camel-ActiveMQ Integration Application started. Press Ctrl+C to stop.");
        main.run(args);
    }
}
