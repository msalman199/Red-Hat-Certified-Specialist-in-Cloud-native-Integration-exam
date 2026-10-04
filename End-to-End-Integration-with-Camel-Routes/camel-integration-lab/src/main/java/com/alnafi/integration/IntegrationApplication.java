package com.alnafi.integration;

import com.alnafi.integration.route.OrderProcessingRoute;
import org.apache.camel.main.Main;

public class IntegrationApplication {
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.configure().addRoutesBuilder(new OrderProcessingRoute());
        main.run(args);
    }
}
