package com.alnafi.integration.route;

import com.alnafi.integration.model.Order;
import com.alnafi.integration.processor.CsvOrderProcessor;
import com.alnafi.integration.processor.CustomerEnrichmentProcessor;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jdbc.JdbcComponent;
import org.apache.camel.model.dataformat.CsvDataFormat;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.Properties;

public class OrderProcessingRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        configureDatabase();

        CsvDataFormat csvFormat = new CsvDataFormat();
        csvFormat.setUseMaps(true);
        csvFormat.setSkipHeaderRecord(true);
        csvFormat.setHeader(new String[]{"order_number", "customer_code", "total_amount"});

        errorHandler(deadLetterChannel("file:data/error")
                .maximumRedeliveries(3)
                .redeliveryDelay(2000)
                .retryAttemptedLogLevel(LoggingLevel.WARN));

        from("file:data/input?move=../processed&moveFailed=../error&noop=false")
                .routeId("order-file-processor")
                .log("Processing order file: ${header.CamelFileName}")
                .unmarshal(csvFormat)
                .process(new CsvOrderProcessor())
                .to("direct:enrich-customer")
                .to("direct:route-order")
                .log("Order processing completed: ${body.orderNumber}");

        from("direct:enrich-customer")
                .routeId("customer-enrichment")
                .log("Enriching customer data for: ${body.customerCode}")
                .enrich("direct:lookup-customer", (original, resource) -> {
                    original.setProperty("customerData", resource.getIn().getBody());
                    return original;
                })
                .process(new CustomerEnrichmentProcessor());

        from("direct:lookup-customer")
                .routeId("customer-lookup")
                .setBody(simple("SELECT customer_name, email FROM customers WHERE customer_code = '${body.customerCode}'"))
                .to("jdbc:dataSource")
                .log("Customer lookup result: ${body}");

        from("direct:route-order")
                .routeId("order-routing")
                .choice()
                    .when(simple("${body.totalAmount} > 1000"))
                        .log("High-value order: ${body.orderNumber}")
                        .setHeader("orderTier", constant("HIGH"))
                    .when(simple("${body.totalAmount} > 500"))
                        .log("Medium-value order: ${body.orderNumber}")
                        .setHeader("orderTier", constant("MEDIUM"))
                    .otherwise()
                        .log("Standard order: ${body.orderNumber}")
                        .setHeader("orderTier", constant("STANDARD"))
                .end()
                .to("direct:save-order")
                .to("direct:generate-report");

        from("direct:save-order")
                .routeId("save-order")
                .process(exchange -> {
                    Order order = exchange.getIn().getBody(Order.class);
                    order.setStatus("PROCESSED");
                })
                .setBody(simple("INSERT INTO orders (order_number, customer_id, total_amount, status) " +
                               "SELECT '${body.orderNumber}', customer_id, ${body.totalAmount}, '${body.status}' " +
                               "FROM customers WHERE customer_code = '${body.customerCode}'"))
                .to("jdbc:dataSource")
                .log("Order saved successfully: ${header.orderTier}");

        from("direct:generate-report")
                .routeId("generate-report")
                .process(exchange -> {
                    Order order = exchange.getIn().getBody(Order.class);
                    String tier = (String) exchange.getIn().getHeader("orderTier");
                    StringBuilder report = new StringBuilder();
                    report.append("ORDER CONFIRMATION\n");
                    report.append("==================\n");
                    report.append("Order Number: ").append(order.getOrderNumber()).append("\n");
                    report.append("Customer: ").append(order.getCustomerName()).append("\n");
                    report.append("Email: ").append(order.getCustomerEmail()).append("\n");
                    report.append("Total Amount: $").append(order.getTotalAmount()).append("\n");
                    report.append("Tier: ").append(tier).append("\n");
                    report.append("Status: ").append(order.getStatus()).append("\n");

                    exchange.getIn().setBody(report.toString());
                    exchange.getIn().setHeader(Exchange.FILE_NAME,
                                             "order_" + order.getOrderNumber() + "_confirmation.txt");
                })
                .to("file:data/output")
                .log("Report generated: ${header.CamelFileName}");
    }

    private void configureDatabase() throws Exception {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            props.load(is);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maxPoolSize", "10")));
        config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minIdle", "2")));

        DataSource dataSource = new HikariDataSource(config);
        JdbcComponent jdbcComponent = new JdbcComponent();
        jdbcComponent.setDataSource(dataSource);
        getContext().addComponent("jdbc", jdbcComponent);
    }
}
