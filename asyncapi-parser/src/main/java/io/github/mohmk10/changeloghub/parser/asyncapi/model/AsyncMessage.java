package io.github.mohmk10.changeloghub.parser.asyncapi.model;

import java.util.*;

public class AsyncMessage {

    private String name;
    private String messageId;
    private String title;
    private String summary;
    private String description;
    private String contentType;
    private AsyncSchema payload;
    private AsyncSchema headers;
    private String correlationId;
    private String schemaFormat;
    private Map<String, Object> bindings;
    private List<Map<String, Object>> examples;
    private List<String> tags;
    private String ref;
    private boolean deprecated;
    private Map<String, Object> extensions;

    public AsyncMessage() {
        this.bindings = new LinkedHashMap<>();
        this.examples = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.extensions = new LinkedHashMap<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public AsyncSchema getPayload() {
        return payload;
    }

    public void setPayload(AsyncSchema payload) {
        this.payload = payload;
    }

    public AsyncSchema getHeaders() {
        return headers;
    }

    public void setHeaders(AsyncSchema headers) {
        this.headers = headers;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getSchemaFormat() {
        return schemaFormat;
    }

    public void setSchemaFormat(String schemaFormat) {
        this.schemaFormat = schemaFormat;
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public void setBindings(Map<String, Object> bindings) {
        this.bindings = bindings != null ? new LinkedHashMap<>(bindings) : new LinkedHashMap<>();
    }

    public List<Map<String, Object>> getExamples() {
        return examples;
    }

    public void setExamples(List<Map<String, Object>> examples) {
        this.examples = examples != null ? new ArrayList<>(examples) : new ArrayList<>();
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
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

    public boolean isReference() {
        return ref != null && !ref.isBlank();
    }

    public boolean hasPayload() {
        return payload != null;
    }

    public boolean hasHeaders() {
        return headers != null;
    }

    public List<String> getRequiredPayloadFields() {
        if (payload == null) {
            return List.of();
        }
        return payload.getRequiredFields();
    }

    public Map<String, AsyncSchema> getPayloadProperties() {
        if (payload == null) {
            return Map.of();
        }
        return payload.getProperties();
    }

    public String getDisplayName() {
        if (title != null && !title.isBlank()) {
            return title;
        }
        if (name != null && !name.isBlank()) {
            return name;
        }
        return messageId;
    }

    public static class Builder {
        private final AsyncMessage message = new AsyncMessage();

        public Builder name(String name) {
            message.setName(name);
            return this;
        }

        public Builder messageId(String messageId) {
            message.setMessageId(messageId);
            return this;
        }

        public Builder title(String title) {
            message.setTitle(title);
            return this;
        }

        public Builder summary(String summary) {
            message.setSummary(summary);
            return this;
        }

        public Builder description(String description) {
            message.setDescription(description);
            return this;
        }

        public Builder contentType(String contentType) {
            message.setContentType(contentType);
            return this;
        }

        public Builder payload(AsyncSchema payload) {
            message.setPayload(payload);
            return this;
        }

        public Builder headers(AsyncSchema headers) {
            message.setHeaders(headers);
            return this;
        }

        public Builder correlationId(String correlationId) {
            message.setCorrelationId(correlationId);
            return this;
        }

        public Builder schemaFormat(String schemaFormat) {
            message.setSchemaFormat(schemaFormat);
            return this;
        }

        public Builder bindings(Map<String, Object> bindings) {
            message.setBindings(bindings);
            return this;
        }

        public Builder tags(List<String> tags) {
            message.setTags(tags);
            return this;
        }

        public Builder ref(String ref) {
            message.setRef(ref);
            return this;
        }

        public Builder deprecated(boolean deprecated) {
            message.setDeprecated(deprecated);
            return this;
        }

        public AsyncMessage build() {
            return message;
        }
    }
}
