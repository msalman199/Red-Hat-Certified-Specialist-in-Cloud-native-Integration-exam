// File: src/main/java/com/example/routing/ConfigurationService.java
package com.example.routing;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationService {

    private static final Map<String, String> routingRules = new HashMap<>();
    private static final Map<String, Integer> priorityWeights = new HashMap<>();
    private static final double HIGH_VALUE_THRESHOLD = 1000.0;

    static {
        routingRules.put("ELECTRONICS", "direct:electronicsProcessor");
        routingRules.put("CLOTHING", "direct:clothingProcessor");
        routingRules.put("BOOKS", "direct:booksProcessor");
        routingRules.put("FOOD", "direct:foodProcessor");

        priorityWeights.put("URGENT", 1);
        priorityWeights.put("HIGH", 2);
        priorityWeights.put("MEDIUM", 3);
        priorityWeights.put("LOW", 4);
    }

    public static String getRouteForCategory(String category) {
        return routingRules.getOrDefault(category.toUpperCase(), "direct:defaultCategoryProcessor");
    }

    public static int getPriorityWeight(String priority) {
        return priorityWeights.getOrDefault(priority.toUpperCase(), 5);
    }

    public static boolean isHighValueOrder(double amount) {
        return amount > HIGH_VALUE_THRESHOLD;
    }

    public static String getProcessingQueue(String region) {
        switch (region.toUpperCase()) {
            case "NORTH":
                return "direct:northRegionQueue";
            case "SOUTH":
                return "direct:southRegionQueue";
            case "EAST":
                return "direct:eastRegionQueue";
            case "WEST":
                return "direct:westRegionQueue";
            default:
                return "direct:defaultRegionQueue";
        }
    }
}
