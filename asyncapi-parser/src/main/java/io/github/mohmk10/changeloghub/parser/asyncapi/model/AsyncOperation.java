package io.github.mohmk10.changeloghub.parser.asyncapi.model;

import io.github.mohmk10.changeloghub.parser.asyncapi.util.OperationType;

import java.util.*;

/**
 * Model representing an AsyncAPI operation (publish/subscribe).
 */
public class AsyncOperation {

    private String operationId;
    private OperationType type;
    private String summary;
    private String description;
    private AsyncMessage message;
    private List<AsyncMessage> messages; // For multiple messages
    private Map<String, Object> bindings;
    private List<String> tags;
    private String channelRef; // For AsyncAPI 3.x
    private Map<String, Object> reply; // For AsyncAPI 3.x
    private List<Map<String, Object>> security;
    private boolean deprecated;
    private Map<String, Object> extensions;

    public AsyncOperation() {
        this.messages = new ArrayList<>();
        this.bindings = new LinkedHashMap<>();
        this.tags = new ArrayList<>();
        this.security = new ArrayList<>();
        this.extensions = new LinkedHashMap<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and setters
    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AsyncMessage getMessage() {
        return message;
    }

    public void setMessage(AsyncMessage message) {
        this.message = message;
    }

    public List<AsyncMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<AsyncMessage> messages) {
        this.messages = messages != null ? new ArrayList<>(messages) : new ArrayList<>();
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public void setBindings(Map<String, Object> bindings) {
        this.bindings = bindings != null ? new LinkedHashMap<>(bindings) : new LinkedHashMap<>();
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    public String getChannelRef() {
        return channelRef;
    }

    public void setChannelRef(String channelRef) {
        this.channelRef = channelRef;
    }

    public Map<String, Object> getReply() {
        return reply;
    }

    public void setReply(Map<String, Object> reply) {
        this.reply = reply;
    }

    public List<Map<String, Object>> getSecurity() {
        return security;
    }

    public void setSecurity(List<Map<String, Object>> security) {
        this.security = security != null ? new ArrayList<>(security) : new ArrayList<>();
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
    public boolean isPublish() {
        return type == OperationType.PUBLISH;
    }

    public boolean isSubscribe() {
        return type == OperationType.SUBSCRIBE;
    }

    public List<AsyncMessage> getAllMessages() {
        List<AsyncMessage> all = new ArrayList<>();
        if (message != null) {
            all.add(message);
        }
        all.addAll(messages);
        return all;
    }

    public boolean hasMessage() {
        return message != null || !messages.isEmpty();
    }

    public boolean hasBindings() {
        return !bindings.isEmpty();
    }

    public String getDisplayName() {
        if (operationId != null && !operationId.isBlank()) {
            return operationId;
        }
        return type != null ? type.name().toLowerCase() : "operation";
    }

    public static class Builder {
        private final AsyncOperation operation = new AsyncOperation();

        public Builder operationId(String operationId) {
            operation.setOperationId(operationId);
            return this;
        }

        public Builder type(OperationType type) {
            operation.setType(type);
            return this;
        }

        public Builder summary(String summary) {
            operation.setSummary(summary);
            return this;
        }

        public Builder description(String description) {
            operation.setDescription(description);
            return this;
        }

        public Builder message(AsyncMessage message) {
            operation.setMessage(message);
            return this;
        }

        public Builder messages(List<AsyncMessage> messages) {
            operation.setMessages(messages);
            return this;
        }

        public Builder addMessage(AsyncMessage message) {
            operation.getMessages().add(message);
            return this;
        }

        public Builder bindings(Map<String, Object> bindings) {
            operation.setBindings(bindings);
            return this;
        }

        public Builder tags(List<String> tags) {
            operation.setTags(tags);
            return this;
        }

        public Builder channelRef(String channelRef) {
            operation.setChannelRef(channelRef);
            return this;
        }

        public Builder reply(Map<String, Object> reply) {
            operation.setReply(reply);
            return this;
        }

        public Builder deprecated(boolean deprecated) {
            operation.setDeprecated(deprecated);
            return this;
        }

        public Builder security(List<Map<String, Object>> security) {
            operation.setSecurity(security);
            return this;
        }

        public Builder extensions(Map<String, Object> extensions) {
            operation.setExtensions(extensions);
            return this;
        }

        public AsyncOperation build() {
            return operation;
        }
    }
}
