package io.github.mohmk10.changeloghub.parser.graphql.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a GraphQL operation (Query, Mutation, or Subscription).
 */
public class GraphQLOperation {

    public enum OperationType {
        QUERY,
        MUTATION,
        SUBSCRIPTION
    }

    private String name;
    private OperationType operationType;
    private String returnType;
    private String description;
    private boolean deprecated;
    private String deprecationReason;
    private boolean returnTypeRequired;
    private boolean returnTypeList;
    private List<GraphQLArgument> arguments = new ArrayList<>();

    public GraphQLOperation() {
    }

    public GraphQLOperation(String name, OperationType operationType, String returnType) {
        this.name = name;
        this.operationType = operationType;
        this.returnType = returnType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public boolean isReturnTypeRequired() {
        return returnTypeRequired;
    }

    public void setReturnTypeRequired(boolean returnTypeRequired) {
        this.returnTypeRequired = returnTypeRequired;
    }

    public boolean isReturnTypeList() {
        return returnTypeList;
    }

    public void setReturnTypeList(boolean returnTypeList) {
        this.returnTypeList = returnTypeList;
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

    public boolean isQuery() {
        return operationType == OperationType.QUERY;
    }

    public boolean isMutation() {
        return operationType == OperationType.MUTATION;
    }

    public boolean isSubscription() {
        return operationType == OperationType.SUBSCRIPTION;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphQLOperation that = (GraphQLOperation) o;
        return Objects.equals(name, that.name) && operationType == that.operationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, operationType);
    }

    @Override
    public String toString() {
        return operationType + " " + name;
    }
}
