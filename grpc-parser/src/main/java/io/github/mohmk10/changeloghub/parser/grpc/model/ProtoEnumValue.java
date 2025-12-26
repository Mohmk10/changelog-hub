package io.github.mohmk10.changeloghub.parser.grpc.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProtoEnumValue {

    private final String name;
    private final int number;
    private final boolean deprecated;
    private final Map<String, String> options;

    private ProtoEnumValue(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "Enum value name is required");
        this.number = builder.number;
        this.deprecated = builder.deprecated;
        this.options = Map.copyOf(builder.options);
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
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

    public boolean hasOption(String key) {
        return options.containsKey(key);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String name, int number) {
        return new Builder().name(name).number(number);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtoEnumValue that = (ProtoEnumValue) o;
        return number == that.number && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, number);
    }

    @Override
    public String toString() {
        return name + " = " + number + (deprecated ? " [deprecated]" : "");
    }

    public static class Builder {
        private String name;
        private int number;
        private boolean deprecated = false;
        private final Map<String, String> options = new HashMap<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder number(int number) {
            this.number = number;
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

        public ProtoEnumValue build() {
            return new ProtoEnumValue(this);
        }
    }
}
