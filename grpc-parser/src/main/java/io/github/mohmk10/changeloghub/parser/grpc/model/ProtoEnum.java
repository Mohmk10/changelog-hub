package io.github.mohmk10.changeloghub.parser.grpc.model;

import java.util.*;

public class ProtoEnum {

    private final String name;
    private final String fullName;
    private final List<ProtoEnumValue> values;
    private final boolean allowAlias;
    private final boolean deprecated;
    private final Map<String, String> options;

    private ProtoEnum(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "Enum name is required");
        this.fullName = builder.fullName != null ? builder.fullName : builder.name;
        this.values = List.copyOf(builder.values);
        this.allowAlias = builder.allowAlias;
        this.deprecated = builder.deprecated;
        this.options = Map.copyOf(builder.options);
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public List<ProtoEnumValue> getValues() {
        return values;
    }

    public Optional<ProtoEnumValue> getValue(String name) {
        return values.stream()
                .filter(v -> v.getName().equals(name))
                .findFirst();
    }

    public Optional<ProtoEnumValue> getValueByNumber(int number) {
        return values.stream()
                .filter(v -> v.getNumber() == number)
                .findFirst();
    }

    public boolean hasValue(String name) {
        return values.stream().anyMatch(v -> v.getName().equals(name));
    }

    public Set<String> getValueNames() {
        Set<String> names = new LinkedHashSet<>();
        values.forEach(v -> names.add(v.getName()));
        return names;
    }

    public boolean isAllowAlias() {
        return allowAlias;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public Map<String, String> getOptions() {
        return options;
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
        ProtoEnum protoEnum = (ProtoEnum) o;
        return Objects.equals(fullName, protoEnum.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName);
    }

    @Override
    public String toString() {
        return "enum " + name + " { " + values.size() + " values }";
    }

    public static class Builder {
        private String name;
        private String fullName;
        private final List<ProtoEnumValue> values = new ArrayList<>();
        private boolean allowAlias = false;
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

        public Builder value(ProtoEnumValue value) {
            this.values.add(value);
            return this;
        }

        public Builder values(List<ProtoEnumValue> values) {
            this.values.addAll(values);
            return this;
        }

        public Builder allowAlias(boolean allowAlias) {
            this.allowAlias = allowAlias;
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

        public ProtoEnum build() {
            return new ProtoEnum(this);
        }
    }
}
