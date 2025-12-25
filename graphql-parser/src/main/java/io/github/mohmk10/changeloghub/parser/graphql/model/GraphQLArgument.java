package io.github.mohmk10.changeloghub.parser.graphql.model;

import java.util.Objects;

/**
 * Represents a GraphQL field argument or input field.
 */
public class GraphQLArgument {

    private String name;
    private String type;
    private String description;
    private boolean required;
    private String defaultValue;
    private boolean deprecated;
    private String deprecationReason;

    public GraphQLArgument() {
    }

    public GraphQLArgument(String name, String type, boolean required) {
        this.name = name;
        this.type = type;
        this.required = required;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getDeprecationReason() {
        return deprecationReason;
    }

    public void setDeprecationReason(String deprecationReason) {
        this.deprecationReason = deprecationReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphQLArgument that = (GraphQLArgument) o;
        return required == that.required &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, required);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(": ").append(type);
        if (defaultValue != null) {
            sb.append(" = ").append(defaultValue);
        }
        return sb.toString();
    }
}
