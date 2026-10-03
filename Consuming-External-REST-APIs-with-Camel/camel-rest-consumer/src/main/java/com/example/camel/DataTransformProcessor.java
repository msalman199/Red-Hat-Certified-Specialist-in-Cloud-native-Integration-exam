package com.example.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;

public class DataTransformProcessor implements Processor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void process(Exchange exchange) throws Exception {
        String jsonBody = exchange.getIn().getBody(String.class);
        JsonNode rootNode = objectMapper.readTree(jsonBody);

        if (rootNode.isArray()) {
            for (JsonNode postNode : rootNode) {
                transformPost((ObjectNode) postNode);
            }
        } else if (rootNode.isObject()) {
            transformPost((ObjectNode) rootNode);
        }

        exchange.getIn().setBody(objectMapper.writeValueAsString(rootNode));
    }

    private void transformPost(ObjectNode postNode) {
        postNode.put("processedAt", Instant.now().toString());
        postNode.put("source", "jsonplaceholder-api");

        if (postNode.has("title")) {
            String title = postNode.get("title").asText();
            postNode.put("title", title.toUpperCase());
        }

        if (postNode.has("body")) {
            String body = postNode.get("body").asText();
            int wordCount = body.trim().isEmpty() ? 0 : body.trim().split("\\s+").length;
            postNode.put("wordCount", wordCount);
        }

        if (postNode.has("userId")) {
            int userId = postNode.get("userId").asInt();
            String category = userId <= 5 ? "PRIORITY" : "STANDARD";
            postNode.put("category", category);
        }
    }
}
