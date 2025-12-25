package io.github.mohmk10.changeloghub.parser.grpc.model;

import java.util.*;

/**
 * Represents a parsed Protocol Buffers file.
 */
public class ProtoFile {

    private final String fileName;
    private final String syntax;
    private final String packageName;
    private final List<String> imports;
    private final List<String> publicImports;
    private final List<ProtoService> services;
    private final List<ProtoMessage> messages;
    private final List<ProtoEnum> enums;
    private final Map<String, String> options;

    private ProtoFile(Builder builder) {
        this.fileName = builder.fileName;
        this.syntax = builder.syntax != null ? builder.syntax : "proto3";
        this.packageName = builder.packageName != null ? builder.packageName : "";
        this.imports = List.copyOf(builder.imports);
        this.publicImports = List.copyOf(builder.publicImports);
        this.services = List.copyOf(builder.services);
        this.messages = List.copyOf(builder.messages);
        this.enums = List.copyOf(builder.enums);
        this.options = Map.copyOf(builder.options);
    }

    public String getFileName() {
        return fileName;
    }

    public String getSyntax() {
        return syntax;
    }

    public boolean isProto3() {
        return "proto3".equals(syntax);
    }

    public boolean isProto2() {
        return "proto2".equals(syntax);
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean hasPackage() {
        return packageName != null && !packageName.isEmpty();
    }

    public List<String> getImports() {
        return imports;
    }

    public List<String> getPublicImports() {
        return publicImports;
    }

    public List<ProtoService> getServices() {
        return services;
    }

    public Optional<ProtoService> getService(String name) {
        return services.stream()
                .filter(s -> s.getName().equals(name))
                .findFirst();
    }

    public boolean hasService(String name) {
        return services.stream().anyMatch(s -> s.getName().equals(name));
    }

    public Set<String> getServiceNames() {
        Set<String> names = new LinkedHashSet<>();
        services.forEach(s -> names.add(s.getName()));
        return names;
    }

    public List<ProtoMessage> getMessages() {
        return messages;
    }

    public Optional<ProtoMessage> getMessage(String name) {
        return messages.stream()
                .filter(m -> m.getName().equals(name))
                .findFirst();
    }

    public boolean hasMessage(String name) {
        return messages.stream().anyMatch(m -> m.getName().equals(name));
    }

    public Set<String> getMessageNames() {
        Set<String> names = new LinkedHashSet<>();
        messages.forEach(m -> names.add(m.getName()));
        return names;
    }

    public List<ProtoEnum> getEnums() {
        return enums;
    }

    public Optional<ProtoEnum> getEnum(String name) {
        return enums.stream()
                .filter(e -> e.getName().equals(name))
                .findFirst();
    }

    public boolean hasEnum(String name) {
        return enums.stream().anyMatch(e -> e.getName().equals(name));
    }

    public Set<String> getEnumNames() {
        Set<String> names = new LinkedHashSet<>();
        enums.forEach(e -> names.add(e.getName()));
        return names;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public String getOption(String key) {
        return options.get(key);
    }

    /**
     * Get the full name of a type including the package prefix.
     */
    public String getFullTypeName(String typeName) {
        if (typeName.contains(".")) {
            return typeName;
        }
        if (hasPackage()) {
            return packageName + "." + typeName;
        }
        return typeName;
    }

    /**
     * Get all RPC methods from all services.
     */
    public List<ProtoRpcMethod> getAllRpcMethods() {
        List<ProtoRpcMethod> allMethods = new ArrayList<>();
        for (ProtoService service : services) {
            allMethods.addAll(service.getMethods());
        }
        return allMethods;
    }

    /**
     * Get all gRPC paths from all services.
     */
    public List<String> getAllGrpcPaths() {
        List<String> paths = new ArrayList<>();
        for (ProtoService service : services) {
            paths.addAll(service.getGrpcPaths(packageName));
        }
        return paths;
    }

    /**
     * Get statistics about this proto file.
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("services", services.size());
        stats.put("messages", messages.size());
        stats.put("enums", enums.size());
        stats.put("rpcMethods", getAllRpcMethods().size());
        stats.put("imports", imports.size());

        int totalFields = 0;
        for (ProtoMessage message : messages) {
            totalFields += message.getFieldCount();
        }
        stats.put("fields", totalFields);

        return stats;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "ProtoFile{" +
                "fileName='" + fileName + '\'' +
                ", syntax='" + syntax + '\'' +
                ", package='" + packageName + '\'' +
                ", services=" + services.size() +
                ", messages=" + messages.size() +
                ", enums=" + enums.size() +
                '}';
    }

    public static class Builder {
        private String fileName;
        private String syntax;
        private String packageName;
        private final List<String> imports = new ArrayList<>();
        private final List<String> publicImports = new ArrayList<>();
        private final List<ProtoService> services = new ArrayList<>();
        private final List<ProtoMessage> messages = new ArrayList<>();
        private final List<ProtoEnum> enums = new ArrayList<>();
        private final Map<String, String> options = new HashMap<>();

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder syntax(String syntax) {
            this.syntax = syntax;
            return this;
        }

        public Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder addImport(String importPath) {
            this.imports.add(importPath);
            return this;
        }

        public Builder imports(List<String> imports) {
            this.imports.addAll(imports);
            return this;
        }

        public Builder addPublicImport(String importPath) {
            this.publicImports.add(importPath);
            return this;
        }

        public Builder publicImports(List<String> publicImports) {
            this.publicImports.addAll(publicImports);
            return this;
        }

        public Builder service(ProtoService service) {
            this.services.add(service);
            return this;
        }

        public Builder services(List<ProtoService> services) {
            this.services.addAll(services);
            return this;
        }

        public Builder message(ProtoMessage message) {
            this.messages.add(message);
            return this;
        }

        public Builder messages(List<ProtoMessage> messages) {
            this.messages.addAll(messages);
            return this;
        }

        public Builder protoEnum(ProtoEnum protoEnum) {
            this.enums.add(protoEnum);
            return this;
        }

        public Builder enums(List<ProtoEnum> enums) {
            this.enums.addAll(enums);
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

        public ProtoFile build() {
            return new ProtoFile(this);
        }
    }
}
