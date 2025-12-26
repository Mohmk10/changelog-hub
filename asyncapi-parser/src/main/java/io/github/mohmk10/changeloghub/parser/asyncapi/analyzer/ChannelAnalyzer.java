package io.github.mohmk10.changeloghub.parser.asyncapi.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncChannel;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncMessage;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncOperation;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncSchema;
import io.github.mohmk10.changeloghub.parser.asyncapi.util.AsyncApiConstants;

import java.util.*;
import java.util.regex.Matcher;

public class ChannelAnalyzer {

    private final OperationAnalyzer operationAnalyzer;
    private final MessageAnalyzer messageAnalyzer;
    private final SchemaAnalyzer schemaAnalyzer;

    public ChannelAnalyzer() {
        this.schemaAnalyzer = new SchemaAnalyzer();
        this.messageAnalyzer = new MessageAnalyzer(schemaAnalyzer);
        this.operationAnalyzer = new OperationAnalyzer(messageAnalyzer);
    }

    public ChannelAnalyzer(OperationAnalyzer operationAnalyzer, MessageAnalyzer messageAnalyzer, SchemaAnalyzer schemaAnalyzer) {
        this.operationAnalyzer = operationAnalyzer;
        this.messageAnalyzer = messageAnalyzer;
        this.schemaAnalyzer = schemaAnalyzer;
    }

    public Map<String, AsyncChannel> analyzeChannels(JsonNode channelsNode) {
        Map<String, AsyncChannel> channels = new LinkedHashMap<>();

        if (channelsNode == null || !channelsNode.isObject()) {
            return channels;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = channelsNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            AsyncChannel channel = analyzeChannel(entry.getKey(), entry.getValue());
            channels.put(entry.getKey(), channel);
        }

        return channels;
    }

    public AsyncChannel analyzeChannel(String name, JsonNode channelNode) {
        AsyncChannel.Builder builder = AsyncChannel.builder()
                .name(name);

        if (channelNode == null) {
            return builder.build();
        }

        if (channelNode.has(AsyncApiConstants.ADDRESS)) {
            builder.address(channelNode.get(AsyncApiConstants.ADDRESS).asText());
        }

        if (channelNode.has(AsyncApiConstants.DESCRIPTION)) {
            builder.description(channelNode.get(AsyncApiConstants.DESCRIPTION).asText());
        }

        if (channelNode.has(AsyncApiConstants.PUBLISH)) {
            AsyncOperation pubOp = operationAnalyzer.analyzePublishOperation(
                    channelNode.get(AsyncApiConstants.PUBLISH));
            builder.publishOperation(pubOp);
        }

        if (channelNode.has(AsyncApiConstants.SUBSCRIBE)) {
            AsyncOperation subOp = operationAnalyzer.analyzeSubscribeOperation(
                    channelNode.get(AsyncApiConstants.SUBSCRIBE));
            builder.subscribeOperation(subOp);
        }

        if (channelNode.has(AsyncApiConstants.PARAMETERS)) {
            builder.parameters(parseParameters(channelNode.get(AsyncApiConstants.PARAMETERS)));
        }

        if (channelNode.has(AsyncApiConstants.BINDINGS)) {
            builder.bindings(parseBindings(channelNode.get(AsyncApiConstants.BINDINGS)));
        }

        if (channelNode.has(AsyncApiConstants.SERVERS)) {
            builder.servers(parseServers(channelNode.get(AsyncApiConstants.SERVERS)));
        }

        if (channelNode.has(AsyncApiConstants.MESSAGES)) {
            builder.messages(parseMessages(channelNode.get(AsyncApiConstants.MESSAGES)));
        }

        if (channelNode.has(AsyncApiConstants.TAGS)) {
            builder.tags(parseTags(channelNode.get(AsyncApiConstants.TAGS)));
        }

        if (channelNode.has(AsyncApiConstants.DEPRECATED)) {
            builder.deprecated(channelNode.get(AsyncApiConstants.DEPRECATED).asBoolean(false));
        }

        return builder.build();
    }

    public List<String> extractChannelParameters(String channelName) {
        List<String> params = new ArrayList<>();
        if (channelName == null) {
            return params;
        }

        Matcher matcher = AsyncApiConstants.CHANNEL_PARAMETER_PATTERN.matcher(channelName);
        while (matcher.find()) {
            params.add(matcher.group(1));
        }
        return params;
    }

    public boolean isDeprecated(JsonNode channelNode) {
        if (channelNode == null) {
            return false;
        }
        return channelNode.has(AsyncApiConstants.DEPRECATED) &&
               channelNode.get(AsyncApiConstants.DEPRECATED).asBoolean(false);
    }

    private Map<String, AsyncChannel.ChannelParameter> parseParameters(JsonNode parametersNode) {
        Map<String, AsyncChannel.ChannelParameter> parameters = new LinkedHashMap<>();

        if (parametersNode == null || !parametersNode.isObject()) {
            return parameters;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = parametersNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            AsyncChannel.ChannelParameter param = parseParameter(entry.getKey(), entry.getValue());
            parameters.put(entry.getKey(), param);
        }

        return parameters;
    }

    private AsyncChannel.ChannelParameter parseParameter(String name, JsonNode paramNode) {
        AsyncChannel.ChannelParameter param = new AsyncChannel.ChannelParameter();
        param.setName(name);

        if (paramNode == null) {
            return param;
        }

        if (paramNode.has(AsyncApiConstants.REF)) {
            param.setRef(paramNode.get(AsyncApiConstants.REF).asText());
            return param;
        }

        if (paramNode.has(AsyncApiConstants.DESCRIPTION)) {
            param.setDescription(paramNode.get(AsyncApiConstants.DESCRIPTION).asText());
        }

        if (paramNode.has("schema")) {
            param.setSchema(schemaAnalyzer.analyzeSchema(paramNode.get("schema")));
        }

        if (paramNode.has("location")) {
            param.setLocation(paramNode.get("location").asText());
        }

        return param;
    }

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

    private List<String> parseServers(JsonNode serversNode) {
        List<String> servers = new ArrayList<>();

        if (serversNode == null) {
            return servers;
        }

        if (serversNode.isArray()) {
            for (JsonNode server : serversNode) {
                if (server.isTextual()) {
                    servers.add(server.asText());
                } else if (server.has(AsyncApiConstants.REF)) {
                    String ref = server.get(AsyncApiConstants.REF).asText();
                    servers.add(AsyncApiConstants.extractRefName(ref));
                }
            }
        }

        return servers;
    }

    private Map<String, AsyncMessage> parseMessages(JsonNode messagesNode) {
        Map<String, AsyncMessage> messages = new LinkedHashMap<>();

        if (messagesNode == null || !messagesNode.isObject()) {
            return messages;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = messagesNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            AsyncMessage message = messageAnalyzer.analyzeMessage(entry.getKey(), entry.getValue());
            if (message != null) {
                messages.put(entry.getKey(), message);
            }
        }

        return messages;
    }

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

    public List<String> getChannelNames(JsonNode channelsNode) {
        List<String> names = new ArrayList<>();
        if (channelsNode != null && channelsNode.isObject()) {
            Iterator<String> fieldNames = channelsNode.fieldNames();
            while (fieldNames.hasNext()) {
                names.add(fieldNames.next());
            }
        }
        return names;
    }
}
