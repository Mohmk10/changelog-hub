package io.github.mohmk10.changeloghub.core.model;

import java.util.Objects;

public class Parameter {

    private String name;
    private ParameterLocation location;
    private String type;
    private boolean required;
    private String defaultValue;
    private String description;

    public Parameter() {
    }

    public Parameter(String name, ParameterLocation location, String type, boolean required,
                     String defaultValue, String description) {
        this.name = name;
        this.location = location;
        this.type = type;
        this.required = required;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ParameterLocation getLocation() {
        return location;
    }

    public void setLocation(ParameterLocation location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parameter parameter = (Parameter) o;
        return required == parameter.required &&
                Objects.equals(name, parameter.name) &&
                location == parameter.location &&
                Objects.equals(type, parameter.type) &&
                Objects.equals(defaultValue, parameter.defaultValue) &&
                Objects.equals(description, parameter.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, location, type, required, defaultValue, description);
    }

    @Override
    public String toString() {
        return "Parameter{" +
                "name='" + name + '\'' +
                ", location=" + location +
                ", type='" + type + '\'' +
                ", required=" + required +
                ", defaultValue='" + defaultValue + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
