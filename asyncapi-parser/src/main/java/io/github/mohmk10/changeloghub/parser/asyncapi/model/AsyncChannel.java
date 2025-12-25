package io.github.mohmk10.changeloghub.parser.asyncapi.model;

import io.github.mohmk10.changeloghub.parser.asyncapi.util.AsyncApiConstants;

import java.util.*;
import java.util.regex.Matcher;

/**
 * Model representing an AsyncAPI channel (topic, queue, etc.).
 */
public class AsyncChannel {

    private String name;
    private String address; // For AsyncAPI 3.x
    private String description;
    private AsyncOperation publishOperation;
    private AsyncOperation subscribeOperation;
    private Map<String, ChannelParameter> parameters;
    private Map<String, Object> bindings;
    private List<String> servers; // Server names this channel is available on
    private Map<String, AsyncMessage> messages; // For AsyncAPI 3.x
    private List<String> tags;
    private boolean deprecated;
    private Map<String, Object> extensions;

    public AsyncChannel() {
        this.parameters = new LinkedHashMap<>();
        this.bindings = new LinkedHashMap<>();
        this.servers = new ArrayList<>();
        this.messages = new LinkedHashMap<>();
        this.tags = new ArrayList<>();
        this.extensions = new LinkedHashMap<>();
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AsyncOperation getPublishOperation() {
        return publishOperation;
    }

    public void setPublishOperation(AsyncOperation publishOperation) {
        this.publishOperation = publishOperation;
    }

    public AsyncOperation getSubscribeOperation() {
        return subscribeOperation;
    }

    public void setSubscribeOperation(AsyncOperation subscribeOperation) {
        this.subscribeOperation = subscribeOperation;
    }

    public Map<String, ChannelParameter> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, ChannelParameter> parameters) {
        this.parameters = parameters != null ? new LinkedHashMap<>(parameters) : new LinkedHashMap<>();
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public void setBindings(Map<String, Object> bindings) {
        this.bindings = bindings != null ? new LinkedHashMap<>(bindings) : new LinkedHashMap<>();
    }

    public List<String> getServers() {
        return servers;
    }

    public void setServers(List<String> servers) {
        this.servers = servers != null ? new ArrayList<>(servers) : new ArrayList<>();
    }

    public Map<String, AsyncMessage> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, AsyncMessage> messages) {
        this.messages = messages != null ? new LinkedHashMap<>(messages) : new LinkedHashMap<>();
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions != null ? new LinkedHashMap<>(extensions) : new LinkedHashMap<>();
    }

    // Utility methods
    public String getChannelPath() {
        return address != null ? address : name;
    }

    public boolean hasPublishOperation() {
        return publishOperation != null;
    }

    public boolean hasSubscribeOperation() {
        return subscribeOperation != null;
    }

    public List<AsyncOperation> getOperations() {
        List<AsyncOperation> ops = new ArrayList<>();
        if (publishOperation != null) {
            ops.add(publishOperation);
        }
        if (subscribeOperation != null) {
            ops.add(subscribeOperation);
        }
        return ops;
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    public boolean hasBindings() {
        return !bindings.isEmpty();
    }

    /**
     * Extract parameter names from the channel name/address.
     */
    public List<String> extractParameterNames() {
        List<String> paramNames = new ArrayList<>();
        String path = getChannelPath();
        if (path == null) {
            return paramNames;
        }

        Matcher matcher = AsyncApiConstants.CHANNEL_PARAMETER_PATTERN.matcher(path);
        while (matcher.find()) {
            paramNames.add(matcher.group(1));
        }
        return paramNames;
    }

    /**
     * Get all messages from operations.
     */
    public List<AsyncMessage> getAllMessages() {
        List<AsyncMessage> allMessages = new ArrayList<>(messages.values());

        if (publishOperation != null) {
            allMessages.addAll(publishOperation.getAllMessages());
        }
        if (subscribeOperation != null) {
            allMessages.addAll(subscribeOperation.getAllMessages());
        }

        return allMessages;
    }

    /**
     * Model for channel parameters.
     */
    public static class ChannelParameter {
        private String name;
        private String description;
        private AsyncSchema schema;
        private String location;
        private String ref;

        public ChannelParameter() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public AsyncSchema getSchema() {
            return schema;
        }

        public void setSchema(AsyncSchema schema) {
            this.schema = schema;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getRef() {
            return ref;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }

        public boolean isReference() {
            return ref != null && !ref.isBlank();
        }
    }

    public static class Builder {
        private final AsyncChannel channel = new AsyncChannel();

        public Builder name(String name) {
            channel.setName(name);
            return this;
        }

        public Builder address(String address) {
            channel.setAddress(address);
            return this;
        }

        public Builder description(String description) {
            channel.setDescription(description);
            return this;
        }

        public Builder publishOperation(AsyncOperation operation) {
            channel.setPublishOperation(operation);
            return this;
        }

        public Builder subscribeOperation(AsyncOperation operation) {
            channel.setSubscribeOperation(operation);
            return this;
        }

        public Builder parameters(Map<String, ChannelParameter> parameters) {
            channel.setParameters(parameters);
            return this;
        }

        public Builder addParameter(String name, ChannelParameter parameter) {
            channel.getParameters().put(name, parameter);
            return this;
        }

        public Builder bindings(Map<String, Object> bindings) {
            channel.setBindings(bindings);
            return this;
        }

        public Builder servers(List<String> servers) {
            channel.setServers(servers);
            return this;
        }

        public Builder messages(Map<String, AsyncMessage> messages) {
            channel.setMessages(messages);
            return this;
        }

        public Builder tags(List<String> tags) {
            channel.setTags(tags);
            return this;
        }

        public Builder deprecated(boolean deprecated) {
            channel.setDeprecated(deprecated);
            return this;
        }

        public AsyncChannel build() {
            return channel;
        }
    }
}
