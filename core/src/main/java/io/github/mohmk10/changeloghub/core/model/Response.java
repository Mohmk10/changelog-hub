package io.github.mohmk10.changeloghub.core.model;

import java.util.Objects;

public class Response {

    private String statusCode;
    private String description;
    private String contentType;
    private String schemaRef;

    public Response() {
    }

    public Response(String statusCode, String description, String contentType, String schemaRef) {
        this.statusCode = statusCode;
        this.description = description;
        this.contentType = contentType;
        this.schemaRef = schemaRef;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getSchemaRef() {
        return schemaRef;
    }

    public void setSchemaRef(String schemaRef) {
        this.schemaRef = schemaRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response response = (Response) o;
        return Objects.equals(statusCode, response.statusCode) &&
                Objects.equals(description, response.description) &&
                Objects.equals(contentType, response.contentType) &&
                Objects.equals(schemaRef, response.schemaRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusCode, description, contentType, schemaRef);
    }

    @Override
    public String toString() {
        return "Response{" +
                "statusCode='" + statusCode + '\'' +
                ", description='" + description + '\'' +
                ", contentType='" + contentType + '\'' +
                ", schemaRef='" + schemaRef + '\'' +
                '}';
    }
}
