package io.github.mohmk10.changeloghub.parser.asyncapi.analyzer;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncServer;
import io.github.mohmk10.changeloghub.parser.asyncapi.util.AsyncApiConstants;
import io.github.mohmk10.changeloghub.parser.asyncapi.util.ProtocolType;

import java.util.*;

/**
 * Analyzer for parsing AsyncAPI server definitions.
 */
public class ServerAnalyzer {

    /**
     * Analyze all servers from the servers node.
     */
    public Map<String, AsyncServer> analyzeServers(JsonNode serversNode) {
        Map<String, AsyncServer> servers = new LinkedHashMap<>();

        if (serversNode == null || !serversNode.isObject()) {
            return servers;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = serversNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            AsyncServer server = analyzeServer(entry.getKey(), entry.getValue());
            servers.put(entry.getKey(), server);
        }

        return servers;
    }

    /**
     * Analyze a single server definition.
     */
    public AsyncServer analyzeServer(String name, JsonNode serverNode) {
        AsyncServer.Builder builder = AsyncServer.builder()
                .name(name);

        if (serverNode == null) {
            return builder.build();
        }

        // URL
        if (serverNode.has(AsyncApiConstants.URL)) {
            builder.url(serverNode.get(AsyncApiConstants.URL).asText());
        }
        // AsyncAPI 3.x uses 'host' instead of 'url'
        if (serverNode.has("host")) {
            String host = serverNode.get("host").asText();
            String pathname = serverNode.has("pathname") ? serverNode.get("pathname").asText() : "";
            builder.url(host + pathname);
        }

        // Protocol
        if (serverNode.has(AsyncApiConstants.PROTOCOL)) {
            String protocolStr = serverNode.get(AsyncApiConstants.PROTOCOL).asText();
            builder.protocol(getProtocol(protocolStr));
        }

        // Protocol version
        if (serverNode.has(AsyncApiConstants.PROTOCOL_VERSION)) {
            builder.protocolVersion(serverNode.get(AsyncApiConstants.PROTOCOL_VERSION).asText());
        }

        // Description
        if (serverNode.has(AsyncApiConstants.DESCRIPTION)) {
            builder.description(serverNode.get(AsyncApiConstants.DESCRIPTION).asText());
        }

        // Variables
        if (serverNode.has(AsyncApiConstants.VARIABLES)) {
            builder.variables(parseVariables(serverNode.get(AsyncApiConstants.VARIABLES)));
        }

        // Bindings
        if (serverNode.has(AsyncApiConstants.BINDINGS)) {
            builder.bindings(parseBindings(serverNode.get(AsyncApiConstants.BINDINGS)));
        }

        // Deprecated
        if (serverNode.has(AsyncApiConstants.DEPRECATED)) {
            builder.deprecated(serverNode.get(AsyncApiConstants.DEPRECATED).asBoolean(false));
        }

        return builder.build();
    }

    /**
     * Get protocol type from string.
     */
    public ProtocolType getProtocol(String protocol) {
        return ProtocolType.fromString(protocol);
    }

    /**
     * Parse server variables.
     */
    private Map<String, AsyncServer.ServerVariable> parseVariables(JsonNode variablesNode) {
        Map<String, AsyncServer.ServerVariable> variables = new LinkedHashMap<>();

        if (variablesNode == null || !variablesNode.isObject()) {
            return variables;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = variablesNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            AsyncServer.ServerVariable variable = parseVariable(entry.getValue());
            variables.put(entry.getKey(), variable);
        }

        return variables;
    }

    /**
     * Parse a single server variable.
     */
    private AsyncServer.ServerVariable parseVariable(JsonNode varNode) {
        AsyncServer.ServerVariable variable = new AsyncServer.ServerVariable();

        if (varNode.has(AsyncApiConstants.DEFAULT)) {
            variable.setDefaultValue(varNode.get(AsyncApiConstants.DEFAULT).asText());
        }

        if (varNode.has(AsyncApiConstants.DESCRIPTION)) {
            variable.setDescription(varNode.get(AsyncApiConstants.DESCRIPTION).asText());
        }

        if (varNode.has(AsyncApiConstants.ENUM)) {
            List<String> allowed = new ArrayList<>();
            for (JsonNode value : varNode.get(AsyncApiConstants.ENUM)) {
                allowed.add(value.asText());
            }
            variable.setAllowedValues(allowed);
        }

        return variable;
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
            bindings.put(entry.getKey(), parseBindingValue(entry.getValue()));
        }

        return bindings;
    }

    /**
     * Parse a binding value recursively.
     */
    private Object parseBindingValue(JsonNode node) {
        if (node.isTextual()) {
            return node.asText();
        } else if (node.isNumber()) {
            return node.numberValue();
        } else if (node.isBoolean()) {
            return node.asBoolean();
        } else if (node.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonNode item : node) {
                list.add(parseBindingValue(item));
            }
            return list;
        } else if (node.isObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                map.put(entry.getKey(), parseBindingValue(entry.getValue()));
            }
            return map;
        }
        return null;
    }

    /**
     * Get server names list.
     */
    public List<String> getServerNames(JsonNode serversNode) {
        List<String> names = new ArrayList<>();
        if (serversNode != null && serversNode.isObject()) {
            Iterator<String> fieldNames = serversNode.fieldNames();
            while (fieldNames.hasNext()) {
                names.add(fieldNames.next());
            }
        }
        return names;
    }

    /**
     * Check if a server uses a secure protocol.
     */
    public boolean isSecure(AsyncServer server) {
        return server.getProtocol() != null && server.getProtocol().isSecure();
    }
}
