package io.github.mohmk10.changeloghub.core.model;

import java.util.Objects;

public class RequestBody {

    private String contentType;
    private String schemaRef;
    private boolean required;

    public RequestBody() {
    }

    public RequestBody(String contentType, String schemaRef, boolean required) {
        this.contentType = contentType;
        this.schemaRef = schemaRef;
        this.required = required;
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

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestBody that = (RequestBody) o;
        return required == that.required &&
                Objects.equals(contentType, that.contentType) &&
                Objects.equals(schemaRef, that.schemaRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentType, schemaRef, required);
    }

    @Override
    public String toString() {
        return "RequestBody{" +
                "contentType='" + contentType + '\'' +
                ", schemaRef='" + schemaRef + '\'' +
                ", required=" + required +
                '}';
    }
}
