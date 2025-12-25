package io.github.mohmk10.changeloghub.parser.grpc.exception;

/**
 * Exception thrown when parsing Protocol Buffer files fails.
 */
public class GrpcParseException extends RuntimeException {

    public GrpcParseException(String message) {
        super(message);
    }

    public GrpcParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public static GrpcParseException invalidProto(String details) {
        return new GrpcParseException("Invalid Protocol Buffer file: " + details);
    }

    public static GrpcParseException fileNotFound(String path) {
        return new GrpcParseException("Protocol Buffer file not found: " + path);
    }

    public static GrpcParseException parseError(String details, Throwable cause) {
        return new GrpcParseException("Failed to parse Protocol Buffer: " + details, cause);
    }

    public static GrpcParseException ioError(String path, Throwable cause) {
        return new GrpcParseException("Failed to read Protocol Buffer file: " + path, cause);
    }

    public static GrpcParseException emptyContent() {
        return new GrpcParseException("Protocol Buffer content is empty or null");
    }

    public static GrpcParseException syntaxError(String details) {
        return new GrpcParseException("Syntax error in Protocol Buffer: " + details);
    }

    public static GrpcParseException unsupportedSyntax(String syntax) {
        return new GrpcParseException("Unsupported Protocol Buffer syntax: " + syntax);
    }
}
