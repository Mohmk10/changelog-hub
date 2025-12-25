package io.github.mohmk10.changeloghub.parser.openapi.exception;

public class OpenApiParseException extends RuntimeException {

    public OpenApiParseException(String message) {
        super(message);
    }

    public OpenApiParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
