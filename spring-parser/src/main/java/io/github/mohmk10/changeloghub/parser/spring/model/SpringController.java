package io.github.mohmk10.changeloghub.parser.spring.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Intermediate model representing a Spring controller.
 */
public class SpringController {

    private String className;
    private String packageName;
    private String basePath;
    private boolean deprecated;
    private List<SpringMethod> methods;
    private List<String> produces;
    private List<String> consumes;

    public SpringController() {
        this.methods = new ArrayList<>();
        this.produces = new ArrayList<>();
        this.consumes = new ArrayList<>();
        this.basePath = "";
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getFullClassName() {
        if (packageName != null && !packageName.isEmpty()) {
            return packageName + "." + className;
        }
        return className;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath != null ? basePath : "";
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public List<SpringMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<SpringMethod> methods) {
        this.methods = methods != null ? methods : new ArrayList<>();
    }

    public void addMethod(SpringMethod method) {
        this.methods.add(method);
    }

    public List<String> getProduces() {
        return produces;
    }

    public void setProduces(List<String> produces) {
        this.produces = produces != null ? produces : new ArrayList<>();
    }

    public List<String> getConsumes() {
        return consumes;
    }

    public void setConsumes(List<String> consumes) {
        this.consumes = consumes != null ? consumes : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "SpringController{" +
                "className='" + className + '\'' +
                ", basePath='" + basePath + '\'' +
                ", methods=" + methods.size() +
                ", deprecated=" + deprecated +
                '}';
    }
}
