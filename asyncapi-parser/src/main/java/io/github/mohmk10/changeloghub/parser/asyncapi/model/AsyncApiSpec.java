package io.github.mohmk10.changeloghub.parser.asyncapi.model;

import io.github.mohmk10.changeloghub.parser.asyncapi.util.AsyncApiVersion;

import java.util.*;

public class AsyncApiSpec {

    private AsyncApiVersion version;
    private String rawVersion;
    private String title;
    private String apiVersion;
    private String description;
    private String termsOfService;
    private Contact contact;
    private License license;
    private String defaultContentType;
    private Map<String, AsyncServer> servers;
    private Map<String, AsyncChannel> channels;
    private Map<String, AsyncOperation> operations; 
    private Components components;
    private List<Tag> tags;
    private ExternalDocs externalDocs;
    private Map<String, Object> extensions;
    private String sourceFile;

    public AsyncApiSpec() {
        this.servers = new LinkedHashMap<>();
        this.channels = new LinkedHashMap<>();
        this.operations = new LinkedHashMap<>();
        this.tags = new ArrayList<>();
        this.extensions = new LinkedHashMap<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public AsyncApiVersion getVersion() {
        return version;
    }

    public void setVersion(AsyncApiVersion version) {
        this.version = version;
    }

    public String getRawVersion() {
        return rawVersion;
    }

    public void setRawVersion(String rawVersion) {
        this.rawVersion = rawVersion;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTermsOfService() {
        return termsOfService;
    }

    public void setTermsOfService(String termsOfService) {
        this.termsOfService = termsOfService;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public String getDefaultContentType() {
        return defaultContentType;
    }

    public void setDefaultContentType(String defaultContentType) {
        this.defaultContentType = defaultContentType;
    }

    public Map<String, AsyncServer> getServers() {
        return servers;
    }

    public void setServers(Map<String, AsyncServer> servers) {
        this.servers = servers != null ? new LinkedHashMap<>(servers) : new LinkedHashMap<>();
    }

    public Map<String, AsyncChannel> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, AsyncChannel> channels) {
        this.channels = channels != null ? new LinkedHashMap<>(channels) : new LinkedHashMap<>();
    }

    public Map<String, AsyncOperation> getOperations() {
        return operations;
    }

    public void setOperations(Map<String, AsyncOperation> operations) {
        this.operations = operations != null ? new LinkedHashMap<>(operations) : new LinkedHashMap<>();
    }

    public Components getComponents() {
        return components;
    }

    public void setComponents(Components components) {
        this.components = components;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    public ExternalDocs getExternalDocs() {
        return externalDocs;
    }

    public void setExternalDocs(ExternalDocs externalDocs) {
        this.externalDocs = externalDocs;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions != null ? new LinkedHashMap<>(extensions) : new LinkedHashMap<>();
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public boolean isV2() {
        return version != null && version.isV2();
    }

    public boolean isV3() {
        return version != null && version.isV3();
    }

    public Optional<AsyncServer> getServer(String name) {
        return Optional.ofNullable(servers.get(name));
    }

    public Optional<AsyncChannel> getChannel(String name) {
        return Optional.ofNullable(channels.get(name));
    }

    public int getChannelCount() {
        return channels.size();
    }

    public int getServerCount() {
        return servers.size();
    }

    public List<AsyncMessage> getAllMessages() {
        List<AsyncMessage> messages = new ArrayList<>();

        for (AsyncChannel channel : channels.values()) {
            messages.addAll(channel.getAllMessages());
        }

        if (components != null && components.getMessages() != null) {
            messages.addAll(components.getMessages().values());
        }

        return messages;
    }

    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("servers", servers.size());
        stats.put("channels", channels.size());
        stats.put("messages", getAllMessages().size());

        int publishOps = 0;
        int subscribeOps = 0;
        for (AsyncChannel channel : channels.values()) {
            if (channel.hasPublishOperation()) publishOps++;
            if (channel.hasSubscribeOperation()) subscribeOps++;
        }
        stats.put("publishOperations", publishOps);
        stats.put("subscribeOperations", subscribeOps);

        if (components != null) {
            stats.put("schemas", components.getSchemas() != null ? components.getSchemas().size() : 0);
        }

        return stats;
    }

    public static class Contact {
        private String name;
        private String url;
        private String email;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class License {
        private String name;
        private String url;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    public static class Tag {
        private String name;
        private String description;
        private ExternalDocs externalDocs;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public ExternalDocs getExternalDocs() { return externalDocs; }
        public void setExternalDocs(ExternalDocs externalDocs) { this.externalDocs = externalDocs; }
    }

    public static class ExternalDocs {
        private String url;
        private String description;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class Components {
        private Map<String, AsyncSchema> schemas;
        private Map<String, AsyncMessage> messages;
        private Map<String, Object> securitySchemes;
        private Map<String, AsyncChannel.ChannelParameter> parameters;
        private Map<String, Object> correlationIds;
        private Map<String, Object> operationTraits;
        private Map<String, Object> messageTraits;
        private Map<String, Object> serverBindings;
        private Map<String, Object> channelBindings;
        private Map<String, Object> operationBindings;
        private Map<String, Object> messageBindings;

        public Components() {
            this.schemas = new LinkedHashMap<>();
            this.messages = new LinkedHashMap<>();
            this.securitySchemes = new LinkedHashMap<>();
            this.parameters = new LinkedHashMap<>();
        }

        public Map<String, AsyncSchema> getSchemas() { return schemas; }
        public void setSchemas(Map<String, AsyncSchema> schemas) {
            this.schemas = schemas != null ? new LinkedHashMap<>(schemas) : new LinkedHashMap<>();
        }

        public Map<String, AsyncMessage> getMessages() { return messages; }
        public void setMessages(Map<String, AsyncMessage> messages) {
            this.messages = messages != null ? new LinkedHashMap<>(messages) : new LinkedHashMap<>();
        }

        public Map<String, Object> getSecuritySchemes() { return securitySchemes; }
        public void setSecuritySchemes(Map<String, Object> securitySchemes) {
            this.securitySchemes = securitySchemes != null ? new LinkedHashMap<>(securitySchemes) : new LinkedHashMap<>();
        }

        public Map<String, AsyncChannel.ChannelParameter> getParameters() { return parameters; }
        public void setParameters(Map<String, AsyncChannel.ChannelParameter> parameters) {
            this.parameters = parameters != null ? new LinkedHashMap<>(parameters) : new LinkedHashMap<>();
        }

        public Map<String, Object> getCorrelationIds() { return correlationIds; }
        public void setCorrelationIds(Map<String, Object> correlationIds) {
            this.correlationIds = correlationIds != null ? new LinkedHashMap<>(correlationIds) : new LinkedHashMap<>();
        }

        public Map<String, Object> getOperationTraits() { return operationTraits; }
        public void setOperationTraits(Map<String, Object> operationTraits) {
            this.operationTraits = operationTraits != null ? new LinkedHashMap<>(operationTraits) : new LinkedHashMap<>();
        }

        public Map<String, Object> getMessageTraits() { return messageTraits; }
        public void setMessageTraits(Map<String, Object> messageTraits) {
            this.messageTraits = messageTraits != null ? new LinkedHashMap<>(messageTraits) : new LinkedHashMap<>();
        }

        public Map<String, Object> getServerBindings() { return serverBindings; }
        public void setServerBindings(Map<String, Object> serverBindings) {
            this.serverBindings = serverBindings != null ? new LinkedHashMap<>(serverBindings) : new LinkedHashMap<>();
        }

        public Map<String, Object> getChannelBindings() { return channelBindings; }
        public void setChannelBindings(Map<String, Object> channelBindings) {
            this.channelBindings = channelBindings != null ? new LinkedHashMap<>(channelBindings) : new LinkedHashMap<>();
        }

        public Map<String, Object> getOperationBindings() { return operationBindings; }
        public void setOperationBindings(Map<String, Object> operationBindings) {
            this.operationBindings = operationBindings != null ? new LinkedHashMap<>(operationBindings) : new LinkedHashMap<>();
        }

        public Map<String, Object> getMessageBindings() { return messageBindings; }
        public void setMessageBindings(Map<String, Object> messageBindings) {
            this.messageBindings = messageBindings != null ? new LinkedHashMap<>(messageBindings) : new LinkedHashMap<>();
        }

        public Optional<AsyncSchema> getSchema(String name) {
            return Optional.ofNullable(schemas.get(name));
        }

        public Optional<AsyncMessage> getMessage(String name) {
            return Optional.ofNullable(messages.get(name));
        }
    }

    public static class Builder {
        private final AsyncApiSpec spec = new AsyncApiSpec();

        public Builder version(AsyncApiVersion version) {
            spec.setVersion(version);
            return this;
        }

        public Builder rawVersion(String rawVersion) {
            spec.setRawVersion(rawVersion);
            return this;
        }

        public Builder title(String title) {
            spec.setTitle(title);
            return this;
        }

        public Builder apiVersion(String apiVersion) {
            spec.setApiVersion(apiVersion);
            return this;
        }

        public Builder description(String description) {
            spec.setDescription(description);
            return this;
        }

        public Builder defaultContentType(String contentType) {
            spec.setDefaultContentType(contentType);
            return this;
        }

        public Builder servers(Map<String, AsyncServer> servers) {
            spec.setServers(servers);
            return this;
        }

        public Builder addServer(String name, AsyncServer server) {
            spec.getServers().put(name, server);
            return this;
        }

        public Builder channels(Map<String, AsyncChannel> channels) {
            spec.setChannels(channels);
            return this;
        }

        public Builder addChannel(String name, AsyncChannel channel) {
            spec.getChannels().put(name, channel);
            return this;
        }

        public Builder operations(Map<String, AsyncOperation> operations) {
            spec.setOperations(operations);
            return this;
        }

        public Builder components(Components components) {
            spec.setComponents(components);
            return this;
        }

        public Builder tags(List<Tag> tags) {
            spec.setTags(tags);
            return this;
        }

        public Builder sourceFile(String sourceFile) {
            spec.setSourceFile(sourceFile);
            return this;
        }

        public Builder asyncApiVersion(AsyncApiVersion version) {
            spec.setVersion(version);
            return this;
        }

        public Builder termsOfService(String termsOfService) {
            spec.setTermsOfService(termsOfService);
            return this;
        }

        public Builder contact(Contact contact) {
            spec.setContact(contact);
            return this;
        }

        public Builder license(License license) {
            spec.setLicense(license);
            return this;
        }

        public Builder externalDocs(ExternalDocs externalDocs) {
            spec.setExternalDocs(externalDocs);
            return this;
        }

        public Builder extensions(Map<String, Object> extensions) {
            spec.setExtensions(extensions);
            return this;
        }

        public AsyncApiSpec build() {
            return spec;
        }
    }

    public AsyncApiVersion getAsyncApiVersion() {
        return version;
    }
}
