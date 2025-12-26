package io.github.mohmk10.changeloghub.parser.graphql.model;

import io.github.mohmk10.changeloghub.parser.graphql.util.GraphQLTypeKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GraphQLType {

    private String name;
    private GraphQLTypeKind kind;
    private String description;
    private boolean deprecated;
    private String deprecationReason;
    private List<GraphQLField> fields = new ArrayList<>();
    private List<String> interfaces = new ArrayList<>();      
    private List<String> possibleTypes = new ArrayList<>();   
    private List<String> enumValues = new ArrayList<>();      

    public GraphQLType() {
    }

    public GraphQLType(String name, GraphQLTypeKind kind) {
        this.name = name;
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GraphQLTypeKind getKind() {
        return kind;
    }

    public void setKind(GraphQLTypeKind kind) {
        this.kind = kind;
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

    public List<GraphQLField> getFields() {
        return fields;
    }

    public void setFields(List<GraphQLField> fields) {
        this.fields = fields != null ? new ArrayList<>(fields) : new ArrayList<>();
    }

    public void addField(GraphQLField field) {
        this.fields.add(field);
    }

    public Optional<GraphQLField> getField(String name) {
        return fields.stream()
                .filter(f -> f.getName().equals(name))
                .findFirst();
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces != null ? new ArrayList<>(interfaces) : new ArrayList<>();
    }

    public void addInterface(String interfaceName) {
        this.interfaces.add(interfaceName);
    }

    public List<String> getPossibleTypes() {
        return possibleTypes;
    }

    public void setPossibleTypes(List<String> possibleTypes) {
        this.possibleTypes = possibleTypes != null ? new ArrayList<>(possibleTypes) : new ArrayList<>();
    }

    public void addPossibleType(String typeName) {
        this.possibleTypes.add(typeName);
    }

    public List<String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<String> enumValues) {
        this.enumValues = enumValues != null ? new ArrayList<>(enumValues) : new ArrayList<>();
    }

    public void addEnumValue(String value) {
        this.enumValues.add(value);
    }

    public boolean isObjectType() {
        return kind == GraphQLTypeKind.OBJECT;
    }

    public boolean isInputType() {
        return kind == GraphQLTypeKind.INPUT_OBJECT;
    }

    public boolean isInterfaceType() {
        return kind == GraphQLTypeKind.INTERFACE;
    }

    public boolean isUnionType() {
        return kind == GraphQLTypeKind.UNION;
    }

    public boolean isEnumType() {
        return kind == GraphQLTypeKind.ENUM;
    }

    public boolean isScalarType() {
        return kind == GraphQLTypeKind.SCALAR;
    }

    public boolean isEnum() {
        return isEnumType();
    }

    public boolean isInterface() {
        return isInterfaceType();
    }

    public boolean isUnion() {
        return isUnionType();
    }

    public boolean isScalar() {
        return isScalarType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphQLType that = (GraphQLType) o;
        return Objects.equals(name, that.name) && kind == that.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, kind);
    }

    @Override
    public String toString() {
        return kind + " " + name;
    }
}
