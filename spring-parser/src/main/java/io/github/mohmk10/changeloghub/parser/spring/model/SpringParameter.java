package io.github.mohmk10.changeloghub.parser.spring.model;

public class SpringParameter {

    public enum Location {
        PATH,
        QUERY,
        HEADER,
        COOKIE,
        BODY
    }

    private String name;
    private String javaType;
    private Location location;
    private boolean required;
    private String defaultValue;
    private String description;

    public SpringParameter() {
        this.required = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
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
    public String toString() {
        return "SpringParameter{" +
                "name='" + name + '\'' +
                ", javaType='" + javaType + '\'' +
                ", location=" + location +
                ", required=" + required +
                '}';
    }
}
