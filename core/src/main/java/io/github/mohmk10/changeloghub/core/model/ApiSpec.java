package io.github.mohmk10.changeloghub.core.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApiSpec {

    private String name;
    private String version;
    private ApiType type;
    private List<Endpoint> endpoints;
    private Map<String, Object> metadata;
    private LocalDateTime parsedAt;

    public ApiSpec() {
        this.endpoints = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    public ApiSpec(String name, String version, ApiType type, List<Endpoint> endpoints,
                   Map<String, Object> metadata, LocalDateTime parsedAt) {
        this.name = name;
        this.version = version;
        this.type = type;
        this.endpoints = endpoints != null ? new ArrayList<>(endpoints) : new ArrayList<>();
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.parsedAt = parsedAt;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ApiType getType() {
        return type;
    }

    public void setType(ApiType type) {
        this.type = type;
    }

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints != null ? new ArrayList<>(endpoints) : new ArrayList<>();
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }

    public LocalDateTime getParsedAt() {
        return parsedAt;
    }

    public void setParsedAt(LocalDateTime parsedAt) {
        this.parsedAt = parsedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiSpec apiSpec = (ApiSpec) o;
        return Objects.equals(name, apiSpec.name) &&
                Objects.equals(version, apiSpec.version) &&
                type == apiSpec.type &&
                Objects.equals(endpoints, apiSpec.endpoints) &&
                Objects.equals(metadata, apiSpec.metadata) &&
                Objects.equals(parsedAt, apiSpec.parsedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, type, endpoints, metadata, parsedAt);
    }

    @Override
    public String toString() {
        return "ApiSpec{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", type=" + type +
                ", endpoints=" + endpoints +
                ", metadata=" + metadata +
                ", parsedAt=" + parsedAt +
                '}';
    }

    public static class Builder {
        private String name;
        private String version;
        private ApiType type;
        private List<Endpoint> endpoints = new ArrayList<>();
        private Map<String, Object> metadata = new HashMap<>();
        private LocalDateTime parsedAt;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder type(ApiType type) {
            this.type = type;
            return this;
        }

        public Builder endpoints(List<Endpoint> endpoints) {
            this.endpoints = endpoints != null ? new ArrayList<>(endpoints) : new ArrayList<>();
            return this;
        }

        public Builder addEndpoint(Endpoint endpoint) {
            this.endpoints.add(endpoint);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
            return this;
        }

        public Builder addMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public Builder parsedAt(LocalDateTime parsedAt) {
            this.parsedAt = parsedAt;
            return this;
        }

        public ApiSpec build() {
            return new ApiSpec(name, version, type, endpoints, metadata, parsedAt);
        }
    }
}
