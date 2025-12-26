package io.github.mohmk10.changeloghub.parser.graphql.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GraphQLField {

    private String name;
    private String type;
    private String description;
    private boolean required;
    private boolean list;
    private boolean listItemRequired;
    private boolean deprecated;
    private String deprecationReason;
    private List<GraphQLArgument> arguments = new ArrayList<>();

    public GraphQLField() {
    }

    public GraphQLField(String name, String type) {
        this.name = name;
        this.type = type;
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

    public boolean isList() {
        return list;
    }

    public void setList(boolean list) {
        this.list = list;
    }

    public boolean isListItemRequired() {
        return listItemRequired;
    }

    public void setListItemRequired(boolean listItemRequired) {
        this.listItemRequired = listItemRequired;
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

    public List<GraphQLArgument> getArguments() {
        return arguments;
    }

    public void setArguments(List<GraphQLArgument> arguments) {
        this.arguments = arguments != null ? new ArrayList<>(arguments) : new ArrayList<>();
    }

    public void addArgument(GraphQLArgument argument) {
        this.arguments.add(argument);
    }

    public boolean hasArguments() {
        return !arguments.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphQLField that = (GraphQLField) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (!arguments.isEmpty()) {
            sb.append("(");
            for (int i = 0; i < arguments.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(arguments.get(i));
            }
            sb.append(")");
        }
        sb.append(": ").append(type);
        if (required) sb.append("!");
        return sb.toString();
    }
}
