package io.github.mohmk10.changeloghub.core.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Endpoint {

    private String id;
    private String path;
    private HttpMethod method;
    private String operationId;
    private String summary;
    private List<Parameter> parameters;
    private RequestBody requestBody;
    private List<Response> responses;
    private boolean deprecated;

    public Endpoint() {
        this.parameters = new ArrayList<>();
        this.responses = new ArrayList<>();
    }

    public Endpoint(String id, String path, HttpMethod method, String operationId, String summary,
                    List<Parameter> parameters, RequestBody requestBody, List<Response> responses,
                    boolean deprecated) {
        this.id = id;
        this.path = path;
        this.method = method;
        this.operationId = operationId;
        this.summary = summary;
        this.parameters = parameters != null ? new ArrayList<>(parameters) : new ArrayList<>();
        this.requestBody = requestBody;
        this.responses = responses != null ? new ArrayList<>(responses) : new ArrayList<>();
        this.deprecated = deprecated;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static String generateId(HttpMethod method, String path) {
        if (method == null || path == null) {
            return null;
        }
        try {
            String input = method.name() + ":" + path;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return method.name() + "_" + path.hashCode();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters != null ? new ArrayList<>(parameters) : new ArrayList<>();
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    public List<Response> getResponses() {
        return responses;
    }

    public void setResponses(List<Response> responses) {
        this.responses = responses != null ? new ArrayList<>(responses) : new ArrayList<>();
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Endpoint endpoint = (Endpoint) o;
        return deprecated == endpoint.deprecated &&
                Objects.equals(id, endpoint.id) &&
                Objects.equals(path, endpoint.path) &&
                method == endpoint.method &&
                Objects.equals(operationId, endpoint.operationId) &&
                Objects.equals(summary, endpoint.summary) &&
                Objects.equals(parameters, endpoint.parameters) &&
                Objects.equals(requestBody, endpoint.requestBody) &&
                Objects.equals(responses, endpoint.responses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path, method, operationId, summary, parameters, requestBody, responses, deprecated);
    }

    @Override
    public String toString() {
        return "Endpoint{" +
                "id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", method=" + method +
                ", operationId='" + operationId + '\'' +
                ", summary='" + summary + '\'' +
                ", parameters=" + parameters +
                ", requestBody=" + requestBody +
                ", responses=" + responses +
                ", deprecated=" + deprecated +
                '}';
    }

    public static class Builder {
        private String id;
        private String path;
        private HttpMethod method;
        private String operationId;
        private String summary;
        private List<Parameter> parameters = new ArrayList<>();
        private RequestBody requestBody;
        private List<Response> responses = new ArrayList<>();
        private boolean deprecated;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder operationId(String operationId) {
            this.operationId = operationId;
            return this;
        }

        public Builder summary(String summary) {
            this.summary = summary;
            return this;
        }

        public Builder parameters(List<Parameter> parameters) {
            this.parameters = parameters != null ? new ArrayList<>(parameters) : new ArrayList<>();
            return this;
        }

        public Builder addParameter(Parameter parameter) {
            this.parameters.add(parameter);
            return this;
        }

        public Builder requestBody(RequestBody requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public Builder responses(List<Response> responses) {
            this.responses = responses != null ? new ArrayList<>(responses) : new ArrayList<>();
            return this;
        }

        public Builder addResponse(Response response) {
            this.responses.add(response);
            return this;
        }

        public Builder deprecated(boolean deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public Endpoint build() {
            if (id == null && method != null && path != null) {
                id = generateId(method, path);
            }
            return new Endpoint(id, path, method, operationId, summary, parameters, requestBody, responses, deprecated);
        }
    }
}
