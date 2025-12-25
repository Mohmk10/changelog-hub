package io.github.mohmk10.changeloghub.parser.grpc.model;

import java.util.*;

/**
 * Represents a Protocol Buffers message definition.
 */
public class ProtoMessage {

    private final String name;
    private final String fullName;
    private final List<ProtoField> fields;
    private final List<ProtoMessage> nestedMessages;
    private final List<ProtoEnum> nestedEnums;
    private final List<String> oneofNames;
    private final Set<Integer> reservedNumbers;
    private final Set<String> reservedNames;
    private final boolean deprecated;
    private final Map<String, String> options;

    private ProtoMessage(Builder builder) {
        this.name = Objects.requireNonNull(builder.name, "Message name is required");
        this.fullName = builder.fullName != null ? builder.fullName : builder.name;
        this.fields = List.copyOf(builder.fields);
        this.nestedMessages = List.copyOf(builder.nestedMessages);
        this.nestedEnums = List.copyOf(builder.nestedEnums);
        this.oneofNames = List.copyOf(builder.oneofNames);
        this.reservedNumbers = Set.copyOf(builder.reservedNumbers);
        this.reservedNames = Set.copyOf(builder.reservedNames);
        this.deprecated = builder.deprecated;
        this.options = Map.copyOf(builder.options);
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public List<ProtoField> getFields() {
        return fields;
    }

    public Optional<ProtoField> getField(String name) {
        return fields.stream()
                .filter(f -> f.getName().equals(name))
                .findFirst();
    }

    public Optional<ProtoField> getFieldByNumber(int number) {
        return fields.stream()
                .filter(f -> f.getNumber() == number)
                .findFirst();
    }

    public boolean hasField(String name) {
        return fields.stream().anyMatch(f -> f.getName().equals(name));
    }

    public Set<String> getFieldNames() {
        Set<String> names = new LinkedHashSet<>();
        fields.forEach(f -> names.add(f.getName()));
        return names;
    }

    public Set<Integer> getFieldNumbers() {
        Set<Integer> numbers = new LinkedHashSet<>();
        fields.forEach(f -> numbers.add(f.getNumber()));
        return numbers;
    }

    public List<ProtoMessage> getNestedMessages() {
        return nestedMessages;
    }

    public Optional<ProtoMessage> getNestedMessage(String name) {
        return nestedMessages.stream()
                .filter(m -> m.getName().equals(name))
                .findFirst();
    }

    public List<ProtoEnum> getNestedEnums() {
        return nestedEnums;
    }

    public Optional<ProtoEnum> getNestedEnum(String name) {
        return nestedEnums.stream()
                .filter(e -> e.getName().equals(name))
                .findFirst();
    }

    public List<String> getOneofNames() {
        return oneofNames;
    }

    public List<ProtoField> getOneofFields(String oneofName) {
        return fields.stream()
                .filter(f -> f.getOneofName().map(n -> n.equals(oneofName)).orElse(false))
                .toList();
    }

    public Set<Integer> getReservedNumbers() {
        return reservedNumbers;
    }

    public Set<String> getReservedNames() {
        return reservedNames;
    }

    public boolean isReserved(int number) {
        return reservedNumbers.contains(number);
    }

    public boolean isReserved(String name) {
        return reservedNames.contains(name);
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public int getFieldCount() {
        return fields.size();
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
        ProtoMessage that = (ProtoMessage) o;
        return Objects.equals(fullName, that.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName);
    }

    @Override
    public String toString() {
        return "message " + name + " { " + fields.size() + " fields }";
    }

    public static class Builder {
        private String name;
        private String fullName;
        private final List<ProtoField> fields = new ArrayList<>();
        private final List<ProtoMessage> nestedMessages = new ArrayList<>();
        private final List<ProtoEnum> nestedEnums = new ArrayList<>();
        private final List<String> oneofNames = new ArrayList<>();
        private final Set<Integer> reservedNumbers = new LinkedHashSet<>();
        private final Set<String> reservedNames = new LinkedHashSet<>();
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

        public Builder field(ProtoField field) {
            this.fields.add(field);
            return this;
        }

        public Builder fields(List<ProtoField> fields) {
            this.fields.addAll(fields);
            return this;
        }

        public Builder nestedMessage(ProtoMessage message) {
            this.nestedMessages.add(message);
            return this;
        }

        public Builder nestedMessages(List<ProtoMessage> messages) {
            this.nestedMessages.addAll(messages);
            return this;
        }

        public Builder nestedEnum(ProtoEnum protoEnum) {
            this.nestedEnums.add(protoEnum);
            return this;
        }

        public Builder nestedEnums(List<ProtoEnum> enums) {
            this.nestedEnums.addAll(enums);
            return this;
        }

        public Builder oneofName(String oneofName) {
            this.oneofNames.add(oneofName);
            return this;
        }

        public Builder oneofNames(List<String> oneofNames) {
            this.oneofNames.addAll(oneofNames);
            return this;
        }

        public Builder reservedNumber(int number) {
            this.reservedNumbers.add(number);
            return this;
        }

        public Builder reservedNumbers(Set<Integer> numbers) {
            this.reservedNumbers.addAll(numbers);
            return this;
        }

        public Builder reservedName(String name) {
            this.reservedNames.add(name);
            return this;
        }

        public Builder reservedNames(Set<String> names) {
            this.reservedNames.addAll(names);
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

        public ProtoMessage build() {
            return new ProtoMessage(this);
        }
    }
}
