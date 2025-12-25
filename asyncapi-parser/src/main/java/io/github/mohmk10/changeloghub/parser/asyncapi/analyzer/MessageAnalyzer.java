package io.github.mohmk10.changeloghub.parser.asyncapi.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncMessage;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncSchema;
import io.github.mohmk10.changeloghub.parser.asyncapi.util.AsyncApiConstants;

import java.util.*;

/**
 * Analyzer for parsing AsyncAPI message definitions.
 */
public class MessageAnalyzer {

    private final SchemaAnalyzer schemaAnalyzer;

    public MessageAnalyzer() {
        this.schemaAnalyzer = new SchemaAnalyzer();
    }

    public MessageAnalyzer(SchemaAnalyzer schemaAnalyzer) {
        this.schemaAnalyzer = schemaAnalyzer;
    }

    /**
     * Analyze a message node.
     */
    public AsyncMessage analyzeMessage(JsonNode messageNode) {
        if (messageNode == null || messageNode.isNull()) {
            return null;
        }

        AsyncMessage.Builder builder = AsyncMessage.builder();

        // Check for reference
        if (messageNode.has(AsyncApiConstants.REF)) {
            builder.ref(messageNode.get(AsyncApiConstants.REF).asText());
            return builder.build();
        }

        // Name
        if (messageNode.has(AsyncApiConstants.NAME)) {
            builder.name(messageNode.get(AsyncApiConstants.NAME).asText());
        }

        // Message ID
        if (messageNode.has(AsyncApiConstants.MESSAGE_ID)) {
            builder.messageId(messageNode.get(AsyncApiConstants.MESSAGE_ID).asText());
        }

        // Title
        if (messageNode.has(AsyncApiConstants.TITLE)) {
            builder.title(messageNode.get(AsyncApiConstants.TITLE).asText());
        }

        // Summary
        if (messageNode.has(AsyncApiConstants.SUMMARY)) {
            builder.summary(messageNode.get(AsyncApiConstants.SUMMARY).asText());
        }

        // Description
        if (messageNode.has(AsyncApiConstants.DESCRIPTION)) {
            builder.description(messageNode.get(AsyncApiConstants.DESCRIPTION).asText());
        }

        // Content type
        if (messageNode.has(AsyncApiConstants.CONTENT_TYPE)) {
            builder.contentType(messageNode.get(AsyncApiConstants.CONTENT_TYPE).asText());
        }

        // Payload
        if (messageNode.has(AsyncApiConstants.PAYLOAD)) {
            builder.payload(schemaAnalyzer.analyzeSchema(messageNode.get(AsyncApiConstants.PAYLOAD)));
        }

        // Headers
        if (messageNode.has(AsyncApiConstants.HEADERS)) {
            builder.headers(schemaAnalyzer.analyzeSchema(messageNode.get(AsyncApiConstants.HEADERS)));
        }

        // Correlation ID
        if (messageNode.has(AsyncApiConstants.CORRELATION_ID)) {
            JsonNode corrIdNode = messageNode.get(AsyncApiConstants.CORRELATION_ID);
            if (corrIdNode.isTextual()) {
                builder.correlationId(corrIdNode.asText());
            } else if (corrIdNode.has("location")) {
                builder.correlationId(corrIdNode.get("location").asText());
            }
        }

        // Schema format
        if (messageNode.has(AsyncApiConstants.SCHEMA_FORMAT)) {
            builder.schemaFormat(messageNode.get(AsyncApiConstants.SCHEMA_FORMAT).asText());
        }

        // Bindings
        if (messageNode.has(AsyncApiConstants.BINDINGS)) {
            builder.bindings(parseBindings(messageNode.get(AsyncApiConstants.BINDINGS)));
        }

        // Tags
        if (messageNode.has(AsyncApiConstants.TAGS)) {
            builder.tags(parseTags(messageNode.get(AsyncApiConstants.TAGS)));
        }

        // Deprecated
        if (messageNode.has(AsyncApiConstants.DEPRECATED)) {
            builder.deprecated(messageNode.get(AsyncApiConstants.DEPRECATED).asBoolean(false));
        }

        return builder.build();
    }

    /**
     * Analyze a named message (with name from components).
     */
    public AsyncMessage analyzeMessage(String name, JsonNode messageNode) {
        AsyncMessage message = analyzeMessage(messageNode);
        if (message != null && message.getName() == null) {
            message.setName(name);
        }
        return message;
    }

    /**
     * Analyze a message reference.
     */
    public AsyncMessage analyzeMessageRef(String ref, JsonNode componentsNode) {
        if (ref == null || componentsNode == null) {
            return AsyncMessage.builder().ref(ref).build();
        }

        // Extract message name from reference
        String messageName = AsyncApiConstants.extractRefName(ref);
        if (messageName == null) {
            return AsyncMessage.builder().ref(ref).build();
        }

        // Try to find the message in components
        if (componentsNode.has(AsyncApiConstants.MESSAGES)) {
            JsonNode messagesNode = componentsNode.get(AsyncApiConstants.MESSAGES);
            if (messagesNode.has(messageName)) {
                AsyncMessage resolved = analyzeMessage(messageName, messagesNode.get(messageName));
                if (resolved != null) {
                    resolved.setRef(ref);
                }
                return resolved;
            }
        }

        return AsyncMessage.builder().ref(ref).name(messageName).build();
    }

    /**
     * Get content type from message node.
     */
    public String getContentType(JsonNode messageNode) {
        if (messageNode == null) {
            return null;
        }
        if (messageNode.has(AsyncApiConstants.CONTENT_TYPE)) {
            return messageNode.get(AsyncApiConstants.CONTENT_TYPE).asText();
        }
        return null;
    }

    /**
     * Get payload schema from message node.
     */
    public JsonNode getPayloadSchema(JsonNode messageNode) {
        if (messageNode == null) {
            return null;
        }
        return messageNode.get(AsyncApiConstants.PAYLOAD);
    }

    /**
     * Get headers from message node.
     */
    public JsonNode getHeaders(JsonNode messageNode) {
        if (messageNode == null) {
            return null;
        }
        return messageNode.get(AsyncApiConstants.HEADERS);
    }

    /**
     * Analyze all messages from components.
     */
    public Map<String, AsyncMessage> analyzeMessages(JsonNode messagesNode) {
        Map<String, AsyncMessage> messages = new LinkedHashMap<>();

        if (messagesNode == null || !messagesNode.isObject()) {
            return messages;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = messagesNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            AsyncMessage message = analyzeMessage(entry.getKey(), entry.getValue());
            if (message != null) {
                messages.put(entry.getKey(), message);
            }
        }

        return messages;
    }

    /**
     * Parse bindings.
     */
    private Map<String, Object> parseBindings(JsonNode bindingsNode) {
        Map<String, Object> bindings = new LinkedHashMap<>();

        if (bindingsNode == null || !bindingsNode.isObject()) {
            return bindings;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = bindingsNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            bindings.put(entry.getKey(), parseValue(entry.getValue()));
        }

        return bindings;
    }

    /**
     * Parse tags.
     */
    private List<String> parseTags(JsonNode tagsNode) {
        List<String> tags = new ArrayList<>();

        if (tagsNode == null || !tagsNode.isArray()) {
            return tags;
        }

        for (JsonNode tag : tagsNode) {
            if (tag.isTextual()) {
                tags.add(tag.asText());
            } else if (tag.has(AsyncApiConstants.NAME)) {
                tags.add(tag.get(AsyncApiConstants.NAME).asText());
            }
        }

        return tags;
    }

    /**
     * Parse any JSON value.
     */
    private Object parseValue(JsonNode node) {
        if (node.isTextual()) {
            return node.asText();
        } else if (node.isNumber()) {
            return node.numberValue();
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode item : node) {
                list.add(parseValue(item));
            }
            return list;
        } else if (node.isObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                map.put(entry.getKey(), parseValue(entry.getValue()));
            }
            return map;
        }
        return null;
    }
}
