package com.alnafi.integration.processor;

import com.alnafi.integration.model.Order;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CsvOrderProcessor implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(CsvOrderProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> csvData = exchange.getIn().getBody(List.class);

        if (csvData == null || csvData.isEmpty()) {
            throw new IllegalArgumentException("CSV data is empty");
        }

        Map<String, String> row = csvData.get(0);
        String orderNumber = row.get("order_number");
        String customerCode = row.get("customer_code");
        BigDecimal totalAmount = new BigDecimal(row.get("total_amount"));

        Order order = new Order(orderNumber, customerCode, totalAmount);

        logger.info("Parsed order: {}", order);

        exchange.getIn().setBody(order);
    }
}
