package io.github.mohmk10.changeloghub.parser.asyncapi.model;

import java.util.*;

public class AsyncSchema {

    private String name;
    private String type;
    private String format;
    private String description;
    private List<String> requiredFields;
    private Map<String, AsyncSchema> properties;
    private AsyncSchema items; 
    private List<String> enumValues;
    private Object defaultValue;
    private String ref;
    private boolean deprecated;
    private Number minimum;
    private Number maximum;
    private Integer minLength;
    private Integer maxLength;
    private String pattern;
    private Boolean additionalProperties;
    private List<AsyncSchema> allOf;
    private List<AsyncSchema> oneOf;
    private List<AsyncSchema> anyOf;
    private Map<String, Object> extensions;

    public AsyncSchema() {
        this.requiredFields = new ArrayList<>();
        this.properties = new LinkedHashMap<>();
        this.enumValues = new ArrayList<>();
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRequiredFields() {
        return requiredFields;
    }

    public void setRequiredFields(List<String> requiredFields) {
        this.requiredFields = requiredFields != null ? new ArrayList<>(requiredFields) : new ArrayList<>();
    }

    public Map<String, AsyncSchema> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, AsyncSchema> properties) {
        this.properties = properties != null ? new LinkedHashMap<>(properties) : new LinkedHashMap<>();
    }

    public AsyncSchema getItems() {
        return items;
    }

    public void setItems(AsyncSchema items) {
        this.items = items;
    }

    public List<String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<String> enumValues) {
        this.enumValues = enumValues != null ? new ArrayList<>(enumValues) : new ArrayList<>();
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
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

    public Number getMinimum() {
        return minimum;
    }

    public void setMinimum(Number minimum) {
        this.minimum = minimum;
    }

    public Number getMaximum() {
        return maximum;
    }

    public void setMaximum(Number maximum) {
        this.maximum = maximum;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Boolean getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Boolean additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public List<AsyncSchema> getAllOf() {
        return allOf;
    }

    public void setAllOf(List<AsyncSchema> allOf) {
        this.allOf = allOf;
    }

    public List<AsyncSchema> getOneOf() {
        return oneOf;
    }

    public void setOneOf(List<AsyncSchema> oneOf) {
        this.oneOf = oneOf;
    }

    public List<AsyncSchema> getAnyOf() {
        return anyOf;
    }

    public void setAnyOf(List<AsyncSchema> anyOf) {
        this.anyOf = anyOf;
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

    public boolean isObject() {
        return "object".equalsIgnoreCase(type);
    }

    public boolean isArray() {
        return "array".equalsIgnoreCase(type);
    }

    public boolean isPrimitive() {
        return type != null && Set.of("string", "number", "integer", "boolean", "null")
                .contains(type.toLowerCase());
    }

    public boolean hasProperty(String propertyName) {
        return properties.containsKey(propertyName);
    }

    public Optional<AsyncSchema> getProperty(String propertyName) {
        return Optional.ofNullable(properties.get(propertyName));
    }

    public boolean isRequired(String fieldName) {
        return requiredFields.contains(fieldName);
    }

    public String getFullType() {
        if (isArray() && items != null) {
            return "array[" + items.getType() + "]";
        }
        if (format != null) {
            return type + "(" + format + ")";
        }
        return type;
    }

    public static class Builder {
        private final AsyncSchema schema = new AsyncSchema();

        public Builder name(String name) {
            schema.setName(name);
            return this;
        }

        public Builder type(String type) {
            schema.setType(type);
            return this;
        }

        public Builder format(String format) {
            schema.setFormat(format);
            return this;
        }

        public Builder description(String description) {
            schema.setDescription(description);
            return this;
        }

        public Builder requiredFields(List<String> required) {
            schema.setRequiredFields(required);
            return this;
        }

        public Builder properties(Map<String, AsyncSchema> properties) {
            schema.setProperties(properties);
            return this;
        }

        public Builder addProperty(String name, AsyncSchema property) {
            schema.getProperties().put(name, property);
            return this;
        }

        public Builder items(AsyncSchema items) {
            schema.setItems(items);
            return this;
        }

        public Builder enumValues(List<String> enumValues) {
            schema.setEnumValues(enumValues);
            return this;
        }

        public Builder defaultValue(Object defaultValue) {
            schema.setDefaultValue(defaultValue);
            return this;
        }

        public Builder ref(String ref) {
            schema.setRef(ref);
            return this;
        }

        public Builder deprecated(boolean deprecated) {
            schema.setDeprecated(deprecated);
            return this;
        }

        public AsyncSchema build() {
            return schema;
        }
    }
}
