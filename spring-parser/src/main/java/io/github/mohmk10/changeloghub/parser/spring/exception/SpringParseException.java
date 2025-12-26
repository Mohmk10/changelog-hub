package io.github.mohmk10.changeloghub.parser.spring.exception;

public class SpringParseException extends RuntimeException {

    public SpringParseException(String message) {
        super(message);
    }

    public SpringParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public static SpringParseException fileNotFound(String path) {
        return new SpringParseException("File not found: " + path);
    }

    public static SpringParseException parseError(String file, Throwable cause) {
        return new SpringParseException("Failed to parse file: " + file, cause);
    }

    public static SpringParseException directoryNotFound(String path) {
        return new SpringParseException("Directory not found: " + path);
    }
}
