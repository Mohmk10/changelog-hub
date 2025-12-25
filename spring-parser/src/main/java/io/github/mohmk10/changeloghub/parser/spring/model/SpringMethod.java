package io.github.mohmk10.changeloghub.parser.spring.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Intermediate model representing a Spring controller method (endpoint).
 */
public class SpringMethod {

    private String methodName;
    private String httpMethod;
    private String path;
    private String summary;
    private boolean deprecated;
    private String responseStatus;
    private String returnType;
    private List<SpringParameter> parameters;
    private List<String> produces;
    private List<String> consumes;

    public SpringMethod() {
        this.parameters = new ArrayList<>();
        this.produces = new ArrayList<>();
        this.consumes = new ArrayList<>();
        this.path = "";
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path != null ? path : "";
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<SpringParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<SpringParameter> parameters) {
        this.parameters = parameters != null ? parameters : new ArrayList<>();
    }

    public void addParameter(SpringParameter parameter) {
        this.parameters.add(parameter);
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
        return "SpringMethod{" +
                "methodName='" + methodName + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", path='" + path + '\'' +
                ", deprecated=" + deprecated +
                ", parameters=" + parameters.size() +
                '}';
    }
}
