package io.github.mohmk10.changeloghub.parser.grpc.model;

import io.github.mohmk10.changeloghub.parser.grpc.util.ProtoFieldRule;
import io.github.mohmk10.changeloghub.parser.grpc.util.ProtoFieldType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ProtoField {

    private final String name;
    private final int number;
    private final String typeName;
    private final ProtoFieldType type;
    private final ProtoFieldRule rule;
    private final String defaultValue;
    private final boolean deprecated;
    private final String oneofName;
    private final String mapKeyType;
    private final String mapValueType;
    private final Map<String, String> options;

    private ProtoField(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "Field name is required");
        this.number = builder.number;
        this.typeName = Objects.requireNonNull(builder.typeName, "Field type is required");
        this.type = builder.type != null ? builder.type : ProtoFieldType.fromString(builder.typeName);
        this.rule = builder.rule != null ? builder.rule : ProtoFieldRule.SINGULAR;
        this.defaultValue = builder.defaultValue;
        this.deprecated = builder.deprecated;
        this.oneofName = builder.oneofName;
        this.mapKeyType = builder.mapKeyType;
        this.mapValueType = builder.mapValueType;
        this.options = Map.copyOf(builder.options);
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public String getTypeName() {
        return typeName;
    }

    public ProtoFieldType getType() {
        return type;
    }

    public ProtoFieldRule getRule() {
        return rule;
    }

    public Optional<String> getDefaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public Optional<String> getOneofName() {
        return Optional.ofNullable(oneofName);
    }

    public boolean isPartOfOneof() {
        return oneofName != null;
    }

    public Optional<String> getMapKeyType() {
        return Optional.ofNullable(mapKeyType);
    }

    public Optional<String> getMapValueType() {
        return Optional.ofNullable(mapValueType);
    }

    public boolean isMap() {
        return rule == ProtoFieldRule.MAP || type == ProtoFieldType.MAP;
    }

    public boolean isRepeated() {
        return rule == ProtoFieldRule.REPEATED;
    }

    public boolean isRequired() {
        return rule == ProtoFieldRule.REQUIRED;
    }

    public boolean isOptional() {
        return rule == ProtoFieldRule.OPTIONAL || rule == ProtoFieldRule.SINGULAR;
    }

    public boolean isScalar() {
        return type.isScalar();
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public String getOption(String key) {
        return options.get(key);
    }

    public String getFullTypeSignature() {
        if (isMap()) {
            return "map<" + mapKeyType + ", " + mapValueType + ">";
        }
        if (rule == ProtoFieldRule.REPEATED) {
            return "repeated " + typeName;
        }
        if (rule == ProtoFieldRule.OPTIONAL) {
            return "optional " + typeName;
        }
        if (rule == ProtoFieldRule.REQUIRED) {
            return "required " + typeName;
        }
        return typeName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String name, int number, String typeName) {
        return new Builder().name(name).number(number).typeName(typeName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtoField that = (ProtoField) o;
        return number == that.number && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, number);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (rule != ProtoFieldRule.SINGULAR) {
            sb.append(rule.getKeyword()).append(" ");
        }
        sb.append(typeName).append(" ").append(name).append(" = ").append(number);
        if (deprecated) {
            sb.append(" [deprecated]");
        }
        return sb.toString();
    }

    public static class Builder {
        private String name;
        private int number;
        private String typeName;
        private ProtoFieldType type;
        private ProtoFieldRule rule;
        private String defaultValue;
        private boolean deprecated = false;
        private String oneofName;
        private String mapKeyType;
        private String mapValueType;
        private final Map<String, String> options = new HashMap<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder number(int number) {
            this.number = number;
            return this;
        }

        public Builder typeName(String typeName) {
            this.typeName = typeName;
            return this;
        }

        public Builder type(ProtoFieldType type) {
            this.type = type;
            return this;
        }

        public Builder rule(ProtoFieldRule rule) {
            this.rule = rule;
            return this;
        }

        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder deprecated(boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public Builder oneofName(String oneofName) {
            this.oneofName = oneofName;
            return this;
        }

        public Builder mapKeyType(String mapKeyType) {
            this.mapKeyType = mapKeyType;
            return this;
        }

        public Builder mapValueType(String mapValueType) {
            this.mapValueType = mapValueType;
            return this;
        }

        public Builder asMap(String keyType, String valueType) {
            this.rule = ProtoFieldRule.MAP;
            this.type = ProtoFieldType.MAP;
            this.mapKeyType = keyType;
            this.mapValueType = valueType;
            this.typeName = "map<" + keyType + ", " + valueType + ">";
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

        public ProtoField build() {
            return new ProtoField(this);
        }
    }
}
