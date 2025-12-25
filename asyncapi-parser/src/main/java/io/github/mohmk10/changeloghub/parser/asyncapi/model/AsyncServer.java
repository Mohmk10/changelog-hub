package io.github.mohmk10.changeloghub.parser.asyncapi.model;

import io.github.mohmk10.changeloghub.parser.asyncapi.util.ProtocolType;

import java.util.*;

/**
 * Model representing an AsyncAPI server (message broker).
 */
public class AsyncServer {

    private String name;
    private String url;
    private ProtocolType protocol;
    private String protocolVersion;
    private String description;
    private Map<String, ServerVariable> variables;
    private List<Map<String, List<String>>> security;
    private Map<String, Object> bindings;
    private Map<String, String> tags;
    private boolean deprecated;

    public AsyncServer() {
        this.variables = new LinkedHashMap<>();
        this.security = new ArrayList<>();
        this.bindings = new LinkedHashMap<>();
        this.tags = new LinkedHashMap<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ProtocolType getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolType protocol) {
        this.protocol = protocol;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, ServerVariable> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, ServerVariable> variables) {
        this.variables = variables != null ? new LinkedHashMap<>(variables) : new LinkedHashMap<>();
    }

    public List<Map<String, List<String>>> getSecurity() {
        return security;
    }

    public void setSecurity(List<Map<String, List<String>>> security) {
        this.security = security != null ? new ArrayList<>(security) : new ArrayList<>();
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public void setBindings(Map<String, Object> bindings) {
        this.bindings = bindings != null ? new LinkedHashMap<>(bindings) : new LinkedHashMap<>();
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags != null ? new LinkedHashMap<>(tags) : new LinkedHashMap<>();
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    // Utility methods
    public String getResolvedUrl() {
        String resolved = url;
        for (Map.Entry<String, ServerVariable> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String defaultValue = entry.getValue().getDefaultValue();
            if (defaultValue != null && resolved.contains(placeholder)) {
                resolved = resolved.replace(placeholder, defaultValue);
            }
        }
        return resolved;
    }

    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    public boolean hasBindings() {
        return !bindings.isEmpty();
    }

    /**
     * Model for server variables.
     */
    public static class ServerVariable {
        private List<String> allowedValues;
        private String defaultValue;
        private String description;

        public ServerVariable() {
            this.allowedValues = new ArrayList<>();
        }

        public List<String> getAllowedValues() {
            return allowedValues;
        }

        public void setAllowedValues(List<String> allowedValues) {
            this.allowedValues = allowedValues != null ? new ArrayList<>(allowedValues) : new ArrayList<>();
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class Builder {
        private final AsyncServer server = new AsyncServer();

        public Builder name(String name) {
            server.setName(name);
            return this;
        }

        public Builder url(String url) {
            server.setUrl(url);
            return this;
        }

        public Builder protocol(ProtocolType protocol) {
            server.setProtocol(protocol);
            return this;
        }

        public Builder protocolVersion(String protocolVersion) {
            server.setProtocolVersion(protocolVersion);
            return this;
        }

        public Builder description(String description) {
            server.setDescription(description);
            return this;
        }

        public Builder variables(Map<String, ServerVariable> variables) {
            server.setVariables(variables);
            return this;
        }

        public Builder bindings(Map<String, Object> bindings) {
            server.setBindings(bindings);
            return this;
        }

        public Builder deprecated(boolean deprecated) {
            server.setDeprecated(deprecated);
            return this;
        }

        public AsyncServer build() {
            return server;
        }
    }
}
