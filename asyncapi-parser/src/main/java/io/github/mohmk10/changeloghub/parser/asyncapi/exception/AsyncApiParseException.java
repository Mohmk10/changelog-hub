package io.github.mohmk10.changeloghub.parser.asyncapi.exception;

/**
 * Exception thrown when parsing AsyncAPI specifications fails.
 */
public class AsyncApiParseException extends RuntimeException {

    private final String location;
    private final String detail;

    public AsyncApiParseException(String message) {
        super(message);
        this.location = null;
        this.detail = null;
    }

    public AsyncApiParseException(String message, Throwable cause) {
        super(message, cause);
        this.location = null;
        this.detail = null;
    }

    public AsyncApiParseException(String message, String location) {
        super(message);
        this.location = location;
        this.detail = null;
    }

    public AsyncApiParseException(String message, String location, String detail) {
        super(message);
        this.location = location;
        this.detail = detail;
    }

    public AsyncApiParseException(String message, String location, Throwable cause) {
        super(message, cause);
        this.location = location;
        this.detail = null;
    }

    public String getLocation() {
        return location;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        if (location != null) {
            sb.append(" at ").append(location);
        }
        if (detail != null) {
            sb.append(": ").append(detail);
        }
        return sb.toString();
    }

    public static AsyncApiParseException invalidVersion(String version) {
        return new AsyncApiParseException("Unsupported AsyncAPI version: " + version);
    }

    public static AsyncApiParseException missingField(String fieldName, String location) {
        return new AsyncApiParseException("Missing required field: " + fieldName, location);
    }

    public static AsyncApiParseException invalidFormat(String message, String location) {
        return new AsyncApiParseException("Invalid format: " + message, location);
    }

    public static AsyncApiParseException unresolvedReference(String ref, String location) {
        return new AsyncApiParseException("Unresolved reference: " + ref, location);
    }
}
