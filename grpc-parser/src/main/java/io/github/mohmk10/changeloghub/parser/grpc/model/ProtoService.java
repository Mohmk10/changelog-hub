package io.github.mohmk10.changeloghub.parser.grpc.model;

import java.util.*;

public class ProtoService {

    private final String name;
    private final String fullName;
    private final List<ProtoRpcMethod> methods;
    private final boolean deprecated;
    private final Map<String, String> options;

    private ProtoService(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "Service name is required");
        this.fullName = builder.fullName != null ? builder.fullName : builder.name;
        this.methods = List.copyOf(builder.methods);
        this.deprecated = builder.deprecated;
        this.options = Map.copyOf(builder.options);
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public List<ProtoRpcMethod> getMethods() {
        return methods;
    }

    public Optional<ProtoRpcMethod> getMethod(String name) {
        return methods.stream()
                .filter(m -> m.getName().equals(name))
                .findFirst();
    }

    public boolean hasMethod(String name) {
        return methods.stream().anyMatch(m -> m.getName().equals(name));
    }

    public Set<String> getMethodNames() {
        Set<String> names = new LinkedHashSet<>();
        methods.forEach(m -> names.add(m.getName()));
        return names;
    }

    public int getMethodCount() {
        return methods.size();
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public String getOption(String key) {
        return options.get(key);
    }

    public List<String> getGrpcPaths(String packageName) {
        List<String> paths = new ArrayList<>();
        for (ProtoRpcMethod method : methods) {
            paths.add(method.getGrpcPath(packageName, name));
        }
        return paths;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String name) {
        return new Builder().name(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtoService that = (ProtoService) o;
        return Objects.equals(fullName, that.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName);
    }

    @Override
    public String toString() {
        return "service " + name + " { " + methods.size() + " methods }";
    }

    public static class Builder {
        private String name;
        private String fullName;
        private final List<ProtoRpcMethod> methods = new ArrayList<>();
        private boolean deprecated = false;
        private final Map<String, String> options = new HashMap<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder method(ProtoRpcMethod method) {
            this.methods.add(method);
            return this;
        }

        public Builder methods(List<ProtoRpcMethod> methods) {
            this.methods.addAll(methods);
            return this;
        }

        public Builder deprecated(boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public Builder option(String key, String value) {
            this.options.put(key, value);
            return this;
        }

        public Builder options(Map<String, String> options) {
            this.options.putAll(options);
            return this;
        }

        public ProtoService build() {
            return new ProtoService(this);
        }
    }
}
