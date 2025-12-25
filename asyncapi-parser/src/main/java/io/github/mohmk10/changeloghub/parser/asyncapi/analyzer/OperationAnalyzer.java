package io.github.mohmk10.changeloghub.parser.asyncapi.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncMessage;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncOperation;
import io.github.mohmk10.changeloghub.parser.asyncapi.util.AsyncApiConstants;
import io.github.mohmk10.changeloghub.parser.asyncapi.util.OperationType;

import java.util.*;

/**
 * Analyzer for parsing AsyncAPI operation definitions.
 */
public class OperationAnalyzer {

    private final MessageAnalyzer messageAnalyzer;

    public OperationAnalyzer() {
        this.messageAnalyzer = new MessageAnalyzer();
    }

    public OperationAnalyzer(MessageAnalyzer messageAnalyzer) {
        this.messageAnalyzer = messageAnalyzer;
    }

    /**
     * Analyze a publish operation (AsyncAPI 2.x).
     */
    public AsyncOperation analyzePublishOperation(JsonNode operationNode) {
        return analyzeOperation(operationNode, OperationType.PUBLISH);
    }

    /**
     * Analyze a subscribe operation (AsyncAPI 2.x).
     */
    public AsyncOperation analyzeSubscribeOperation(JsonNode operationNode) {
        return analyzeOperation(operationNode, OperationType.SUBSCRIBE);
    }

    /**
     * Analyze an operation with type.
     */
    public AsyncOperation analyzeOperation(JsonNode operationNode, OperationType type) {
        if (operationNode == null || operationNode.isNull()) {
            return null;
        }

        AsyncOperation.Builder builder = AsyncOperation.builder()
                .type(type);

        // Operation ID
        if (operationNode.has(AsyncApiConstants.OPERATION_ID)) {
            builder.operationId(operationNode.get(AsyncApiConstants.OPERATION_ID).asText());
        }

        // Summary
        if (operationNode.has(AsyncApiConstants.SUMMARY)) {
            builder.summary(operationNode.get(AsyncApiConstants.SUMMARY).asText());
        }

        // Description
        if (operationNode.has(AsyncApiConstants.DESCRIPTION)) {
            builder.description(operationNode.get(AsyncApiConstants.DESCRIPTION).asText());
        }

        // Message (single)
        if (operationNode.has(AsyncApiConstants.MESSAGE)) {
            JsonNode messageNode = operationNode.get(AsyncApiConstants.MESSAGE);
            // Check if it's a oneOf (multiple messages)
            if (messageNode.has(AsyncApiConstants.ONE_OF)) {
                List<AsyncMessage> messages = parseMultipleMessages(messageNode.get(AsyncApiConstants.ONE_OF));
                builder.messages(messages);
            } else {
                builder.message(messageAnalyzer.analyzeMessage(messageNode));
            }
        }

        // Messages (AsyncAPI 3.x)
        if (operationNode.has(AsyncApiConstants.MESSAGES)) {
            JsonNode messagesNode = operationNode.get(AsyncApiConstants.MESSAGES);
            if (messagesNode.isArray()) {
                builder.messages(parseMultipleMessages(messagesNode));
            }
        }

        // Bindings
        if (operationNode.has(AsyncApiConstants.BINDINGS)) {
            builder.bindings(parseBindings(operationNode.get(AsyncApiConstants.BINDINGS)));
        }

        // Tags
        if (operationNode.has(AsyncApiConstants.TAGS)) {
            builder.tags(parseTags(operationNode.get(AsyncApiConstants.TAGS)));
        }

        // Security
        if (operationNode.has(AsyncApiConstants.SECURITY)) {
            builder.security(parseSecurity(operationNode.get(AsyncApiConstants.SECURITY)));
        }

        // Deprecated
        if (operationNode.has(AsyncApiConstants.DEPRECATED)) {
            builder.deprecated(operationNode.get(AsyncApiConstants.DEPRECATED).asBoolean(false));
        }

        return builder.build();
    }

    /**
     * Analyze an AsyncAPI 3.x operation.
     */
    public AsyncOperation analyzeOperationV3(String operationId, JsonNode operationNode) {
        if (operationNode == null || operationNode.isNull()) {
            return null;
        }

        AsyncOperation.Builder builder = AsyncOperation.builder()
                .operationId(operationId);

        // Action (send/receive)
        if (operationNode.has(AsyncApiConstants.ACTION)) {
            String action = operationNode.get(AsyncApiConstants.ACTION).asText();
            builder.type(OperationType.fromV3Action(action));
        }

        // Channel reference
        if (operationNode.has(AsyncApiConstants.CHANNEL)) {
            JsonNode channelNode = operationNode.get(AsyncApiConstants.CHANNEL);
            if (channelNode.has(AsyncApiConstants.REF)) {
                builder.channelRef(channelNode.get(AsyncApiConstants.REF).asText());
            }
        }

        // Summary
        if (operationNode.has(AsyncApiConstants.SUMMARY)) {
            builder.summary(operationNode.get(AsyncApiConstants.SUMMARY).asText());
        }

        // Description
        if (operationNode.has(AsyncApiConstants.DESCRIPTION)) {
            builder.description(operationNode.get(AsyncApiConstants.DESCRIPTION).asText());
        }

        // Messages
        if (operationNode.has(AsyncApiConstants.MESSAGES)) {
            JsonNode messagesNode = operationNode.get(AsyncApiConstants.MESSAGES);
            builder.messages(parseMultipleMessages(messagesNode));
        }

        // Reply (for request-reply patterns)
        if (operationNode.has(AsyncApiConstants.REPLY)) {
            builder.reply(parseReply(operationNode.get(AsyncApiConstants.REPLY)));
        }

        // Bindings
        if (operationNode.has(AsyncApiConstants.BINDINGS)) {
            builder.bindings(parseBindings(operationNode.get(AsyncApiConstants.BINDINGS)));
        }

        // Tags
        if (operationNode.has(AsyncApiConstants.TAGS)) {
            builder.tags(parseTags(operationNode.get(AsyncApiConstants.TAGS)));
        }

        // Deprecated
        if (operationNode.has(AsyncApiConstants.DEPRECATED)) {
            builder.deprecated(operationNode.get(AsyncApiConstants.DEPRECATED).asBoolean(false));
        }

        return builder.build();
    }

    /**
     * Analyze all AsyncAPI 3.x operations.
     */
    public Map<String, AsyncOperation> analyzeOperationsV3(JsonNode operationsNode) {
        Map<String, AsyncOperation> operations = new LinkedHashMap<>();

        if (operationsNode == null || !operationsNode.isObject()) {
            return operations;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = operationsNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            AsyncOperation operation = analyzeOperationV3(entry.getKey(), entry.getValue());
            if (operation != null) {
                operations.put(entry.getKey(), operation);
            }
        }

        return operations;
    }

    /**
     * Parse multiple messages (oneOf).
     */
    private List<AsyncMessage> parseMultipleMessages(JsonNode messagesNode) {
        List<AsyncMessage> messages = new ArrayList<>();

        if (messagesNode == null) {
            return messages;
        }

        if (messagesNode.isArray()) {
            for (JsonNode msgNode : messagesNode) {
                AsyncMessage msg = messageAnalyzer.analyzeMessage(msgNode);
                if (msg != null) {
                    messages.add(msg);
                }
            }
        } else if (messagesNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = messagesNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                AsyncMessage msg = messageAnalyzer.analyzeMessage(entry.getKey(), entry.getValue());
                if (msg != null) {
                    messages.add(msg);
                }
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
     * Parse security.
     */
    private List<Map<String, Object>> parseSecurity(JsonNode securityNode) {
        List<Map<String, Object>> security = new ArrayList<>();

        if (securityNode == null || !securityNode.isArray()) {
            return security;
        }

        for (JsonNode secNode : securityNode) {
            if (secNode.isObject()) {
                Map<String, Object> secItem = new LinkedHashMap<>();
                Iterator<Map.Entry<String, JsonNode>> fields = secNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    secItem.put(entry.getKey(), parseValue(entry.getValue()));
                }
                security.add(secItem);
            }
        }

        return security;
    }

    /**
     * Parse reply configuration.
     */
    private Map<String, Object> parseReply(JsonNode replyNode) {
        Map<String, Object> reply = new LinkedHashMap<>();

        if (replyNode == null || !replyNode.isObject()) {
            return reply;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = replyNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            reply.put(entry.getKey(), parseValue(entry.getValue()));
        }

        return reply;
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
